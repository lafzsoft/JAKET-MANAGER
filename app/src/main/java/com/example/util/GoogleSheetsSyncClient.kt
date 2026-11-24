package com.example.util

import com.example.model.DailyProductionItem
import com.example.model.JacketItem
import com.example.model.KasbonItem
import com.example.model.KasbonStatus
import com.example.model.ProductionShift
import com.example.model.ProductionStatus
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

data class SheetsPingResult(
    val success: Boolean,
    val message: String,
    val spreadsheetName: String = "",
    val spreadsheetUrl: String = "",
    val itemCount: Int = 0,
    val version: String = ""
)

data class SheetsPushResult(
    val success: Boolean,
    val inserted: Int = 0,
    val updated: Int = 0,
    val total: Int = 0,
    val spreadsheetUrl: String = "",
    val message: String = ""
)

data class SheetsPullResult(
    val success: Boolean,
    val jackets: List<JacketItem> = emptyList(),
    val spreadsheetUrl: String = "",
    val message: String = ""
)

data class SheetsRepairResult(
    val success: Boolean,
    val repairedSheets: List<String> = emptyList(),
    val spreadsheetUrl: String = "",
    val message: String = ""
)

data class SheetsFullPushResult(
    val success: Boolean,
    val jacketsCount: Int = 0,
    val productionsCount: Int = 0,
    val kasbonCount: Int = 0,
    val spreadsheetUrl: String = "",
    val timestamp: String = "",
    val message: String = ""
)

data class SheetsFullPullResult(
    val success: Boolean,
    val jackets: List<JacketItem> = emptyList(),
    val productions: List<DailyProductionItem> = emptyList(),
    val kasbon: List<KasbonItem> = emptyList(),
    val spreadsheetUrl: String = "",
    val timestamp: String = "",
    val message: String = ""
)

/**
 * Real HTTP client for synchronizing Android inventory with Google Sheets
 * via a deployed Google Apps Script Web App (`google_apps_script/Code.gs`).
 */
object GoogleSheetsSyncClient {

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val moshi: Moshi = Moshi.Builder().build()

    private val mapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Any::class.java
    )

    private val mapAdapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(mapType)

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Validates that a user-supplied URL points to a deployed Google Apps Script Web App (`/exec`).
     */
    fun isValidAppsScriptUrl(rawUrl: String): Boolean {
        val url = rawUrl.trim()
        if (url.isBlank()) return false
        val isGoogleScriptHost =
            url.startsWith("https://script.google.com/macros/s/", ignoreCase = true) ||
            url.startsWith("https://script.google.com/a/macros/", ignoreCase = true) ||
            url.startsWith("https://script.googleusercontent.com/", ignoreCase = true)
        if (!isGoogleScriptHost) return false
        if (url.contains("/dev", ignoreCase = true) && !url.contains("/exec", ignoreCase = true)) {
            return false
        }
        return url.contains("/exec", ignoreCase = true) ||
            url.startsWith("https://script.googleusercontent.com/", ignoreCase = true)
    }

    /**
     * Builds the `GET` URL with an action query parameter safely appended.
     */
    fun buildActionUrl(webAppUrl: String, action: String): String {
        val clean = webAppUrl.trim()
        val separator = if (clean.contains("?")) "&" else "?"
        return "$clean${separator}action=$action"
    }

    /**
     * Serializes the batch sync payload expected by `Code.gs` (`action: "syncInventory"`).
     */
    fun buildSyncPayloadJson(jackets: List<JacketItem>): String {
        val jacketPayloads = jackets.map { item ->
            linkedMapOf<String, Any?>(
                "id" to item.id,
                "sku" to item.sku,
                "name" to item.name,
                "genderCategory" to item.genderCategory,
                "style" to item.style,
                "size" to item.size,
                "quantity" to item.quantity,
                "minThreshold" to item.minThreshold,
                "material" to item.material,
                "color" to item.color,
                "locationRack" to item.locationRack,
                "unitPrice" to CurrencyUtils.normalizeRupiah(item.unitPrice),
                "imageUrl" to item.imageUrl,
                "isSeasonal" to item.isSeasonal,
                "description" to item.description
            )
        }
        val payload = linkedMapOf<String, Any?>(
            "action" to "syncInventory",
            "jackets" to jacketPayloads
        )
        return mapAdapter.toJson(payload)
    }

    /**
     * Builds the JSON payload to invoke `repairAndFormatSpreadsheet()` on Google Apps Script.
     */
    fun buildRepairPayloadJson(): String {
        return mapAdapter.toJson(mapOf("action" to "repairSpreadsheet"))
    }

    /**
     * Serializes the full multi-sheet sync payload expected by `Code.gs` (`action: "syncAllData"`).
     */
    fun buildFullSyncPayloadJson(
        jackets: List<JacketItem>,
        productions: List<DailyProductionItem>,
        kasbonList: List<KasbonItem>,
        replaceMissing: Boolean = true
    ): String {
        val jacketPayloads = jackets.map { item ->
            linkedMapOf<String, Any?>(
                "id" to item.id,
                "sku" to item.sku,
                "name" to item.name,
                "genderCategory" to item.genderCategory,
                "style" to item.style,
                "size" to item.size,
                "quantity" to item.quantity,
                "minThreshold" to item.minThreshold,
                "material" to item.material,
                "color" to item.color,
                "locationRack" to item.locationRack,
                "unitPrice" to CurrencyUtils.normalizeRupiah(item.unitPrice),
                "imageUrl" to item.imageUrl,
                "isSeasonal" to item.isSeasonal,
                "description" to item.description
            )
        }
        val prodPayloads = productions.map { p ->
            linkedMapOf<String, Any?>(
                "id" to p.id,
                "date" to p.date,
                "dateString" to p.dateString,
                "jacketId" to p.jacketId,
                "jacketName" to p.jacketName,
                "jacketSku" to p.jacketSku,
                "batchNumber" to p.batchNumber,
                "shift" to p.shift.name,
                "operatorName" to p.operatorName,
                "quantityProduced" to p.quantityProduced,
                "defectCount" to p.defectCount,
                "status" to p.status.name,
                "notes" to p.notes,
                "addedToStock" to p.addedToStock,
                "createdAt" to p.createdAt
            )
        }
        val kasbonPayloads = kasbonList.map { k ->
            linkedMapOf<String, Any?>(
                "id" to k.id,
                "date" to k.date,
                "dateString" to k.dateString,
                "employeeName" to k.employeeName,
                "role" to k.role,
                "amount" to CurrencyUtils.normalizeRupiah(k.amount),
                "paidAmount" to k.paidAmount,
                "status" to k.status.name,
                "purpose" to k.purpose,
                "repaymentMethod" to k.repaymentMethod,
                "dueDate" to k.dueDate,
                "notes" to k.notes
            )
        }
        val payload = linkedMapOf<String, Any?>(
            "action" to "syncAllData",
            "replaceMissing" to replaceMissing,
            "jackets" to jacketPayloads,
            "productions" to prodPayloads,
            "kasbon" to kasbonPayloads
        )
        return mapAdapter.toJson(payload)
    }

    /**
     * Detects when Google Apps Script redirected to an HTML login or error page instead of JSON.
     */
    fun detectHtmlAuthError(body: String): String? {
        val trimmed = body.trimStart()
        val looksLikeHtml = trimmed.startsWith("<!DOCTYPE", ignoreCase = true) ||
            trimmed.startsWith("<html", ignoreCase = true) ||
            trimmed.startsWith("<head", ignoreCase = true) ||
            trimmed.contains("ServiceLogin", ignoreCase = true) ||
            trimmed.contains("accounts.google.com", ignoreCase = true)

        return if (looksLikeHtml) {
            "Google Apps Script mengembalikan halaman HTML/login, bukan JSON. Pastikan Web App di-deploy dengan 'Who has access: Anyone (Siapa saja)' dan gunakan URL berakhiran /exec."
        } else {
            null
        }
    }

    fun parsePingResponse(body: String): SheetsPingResult {
        detectHtmlAuthError(body)?.let { return SheetsPingResult(success = false, message = it) }
        val map = parseJsonObject(body)
            ?: return SheetsPingResult(success = false, message = "Format respons server tidak valid (bukan JSON).")

        val success = asBoolean(map["success"], false)
        val message = asString(map["message"]).ifBlank {
            asString(map["error"]).ifBlank {
                if (success) "Koneksi ke Google Apps Script berhasil." else "Gagal terhubung ke Google Apps Script."
            }
        }
        return SheetsPingResult(
            success = success,
            message = message,
            spreadsheetName = asString(map["spreadsheetName"]),
            spreadsheetUrl = asString(map["spreadsheetUrl"]),
            itemCount = asInt(map["itemCount"], 0),
            version = asString(map["version"])
        )
    }

    fun parsePushResponse(body: String): SheetsPushResult {
        detectHtmlAuthError(body)?.let { return SheetsPushResult(success = false, message = it) }
        val map = parseJsonObject(body)
            ?: return SheetsPushResult(success = false, message = "Respons sinkronisasi tidak valid (bukan JSON).")

        val success = asBoolean(map["success"], false)
        val inserted = asInt(map["inserted"], 0)
        val updated = asInt(map["updated"], 0)
        val total = asInt(map["total"], 0)
        val spreadsheetUrl = asString(map["spreadsheetUrl"])
        val message = asString(map["message"]).ifBlank {
            asString(map["error"]).ifBlank {
                if (success) {
                    "Sinkronisasi selesai: $updated diperbarui, $inserted ditambahkan (total $total baris)."
                } else {
                    "Gagal mengirim data ke Google Sheets."
                }
            }
        }
        return SheetsPushResult(
            success = success,
            inserted = inserted,
            updated = updated,
            total = total,
            spreadsheetUrl = spreadsheetUrl,
            message = message
        )
    }

    fun parsePullResponse(body: String): SheetsPullResult {
        detectHtmlAuthError(body)?.let { return SheetsPullResult(success = false, message = it) }
        val map = parseJsonObject(body)
            ?: return SheetsPullResult(success = false, message = "Respons pengambilan data tidak valid (bukan JSON).")

        val success = asBoolean(map["success"], false)
        val spreadsheetUrl = asString(map["spreadsheetUrl"])
        if (!success) {
            return SheetsPullResult(
                success = false,
                spreadsheetUrl = spreadsheetUrl,
                message = asString(map["error"]).ifBlank { "Gagal mengambil data dari Google Sheets." }
            )
        }

        val rawList = map["jackets"] as? List<*> ?: emptyList<Any?>()
        val parsedItems = rawList.mapIndexedNotNull { index, raw ->
            val obj = raw as? Map<*, *> ?: return@mapIndexedNotNull null
            val sku = asString(obj["sku"]).trim()
            val name = asString(obj["name"]).trim()
            val id = asString(obj["id"]).trim().ifBlank { "sheet_${sku.ifBlank { index.toString() }}" }
            if (sku.isBlank() && name.isBlank()) return@mapIndexedNotNull null

            val rawPrice = asDouble(obj["unitPrice"], 350_000.0)
            JacketItem(
                id = id,
                sku = sku.ifBlank { "SKU-${index + 1}" },
                name = name.ifBlank { "Jaket" },
                style = asString(obj["style"]).ifBlank { "Bomber Jacket" },
                size = asString(obj["size"]).ifBlank { "L" },
                material = asString(obj["material"]).ifBlank { "Cotton" },
                color = asString(obj["color"]).ifBlank { "Black" },
                quantity = asInt(obj["quantity"], 0).coerceAtLeast(0),
                minThreshold = asInt(obj["minThreshold"], 5).coerceAtLeast(1),
                locationRack = asString(obj["locationRack"]).ifBlank { "Rack A-01" },
                imageUrl = asString(obj["imageUrl"]),
                isSeasonal = asBoolean(obj["isSeasonal"], false),
                unitPrice = CurrencyUtils.normalizeRupiah(rawPrice),
                genderCategory = asString(obj["genderCategory"]).ifBlank { "Jaket Pria" },
                description = asString(obj["description"])
            )
        }

        val msg = "Berhasil menarik ${parsedItems.size} item inventaris dari Google Sheets."
        return SheetsPullResult(
            success = true,
            jackets = parsedItems,
            spreadsheetUrl = spreadsheetUrl,
            message = msg
        )
    }

    fun parseRepairResponse(body: String): SheetsRepairResult {
        detectHtmlAuthError(body)?.let { return SheetsRepairResult(success = false, message = it) }
        val map = parseJsonObject(body)
            ?: return SheetsRepairResult(success = false, message = "Respons perbaikan spreadsheet tidak valid.")

        val success = asBoolean(map["success"], false)
        val rawSheets = map["repairedSheets"] as? List<*> ?: emptyList<Any?>()
        val sheets = rawSheets.mapNotNull { it?.toString()?.takeIf { s -> s.isNotBlank() } }
        val message = asString(map["message"]).ifBlank {
            asString(map["error"]).ifBlank {
                if (success) "Struktur dan format Spreadsheet berhasil diperbaiki." else "Gagal memperbaiki Spreadsheet."
            }
        }
        return SheetsRepairResult(
            success = success,
            repairedSheets = sheets,
            spreadsheetUrl = asString(map["spreadsheetUrl"]),
            message = message
        )
    }

    fun parseFullPushResponse(body: String): SheetsFullPushResult {
        detectHtmlAuthError(body)?.let { return SheetsFullPushResult(success = false, message = it) }
        val map = parseJsonObject(body)
            ?: return SheetsFullPushResult(success = false, message = "Respons sinkronisasi penuh tidak valid (bukan JSON).")

        val success = asBoolean(map["success"], false)
        val jacketsCount = asInt(map["jacketsCount"], asInt(map["total"], 0))
        val productionsCount = asInt(map["productionsCount"], 0)
        val kasbonCount = asInt(map["kasbonCount"], 0)
        val spreadsheetUrl = asString(map["spreadsheetUrl"])
        val timestamp = asString(map["timestamp"])
        val message = asString(map["message"]).ifBlank {
            asString(map["error"]).ifBlank {
                if (success) {
                    "Sinkronisasi selesai: $jacketsCount jaket, $productionsCount produksi, $kasbonCount kasbon."
                } else {
                    "Gagal menyinkronkan data ke Google Sheets."
                }
            }
        }
        return SheetsFullPushResult(
            success = success,
            jacketsCount = jacketsCount,
            productionsCount = productionsCount,
            kasbonCount = kasbonCount,
            spreadsheetUrl = spreadsheetUrl,
            timestamp = timestamp,
            message = message
        )
    }

    fun parseFullPullResponse(body: String): SheetsFullPullResult {
        detectHtmlAuthError(body)?.let { return SheetsFullPullResult(success = false, message = it) }
        val map = parseJsonObject(body)
            ?: return SheetsFullPullResult(success = false, message = "Respons pengambilan data tidak valid (bukan JSON).")

        val success = asBoolean(map["success"], false)
        val spreadsheetUrl = asString(map["spreadsheetUrl"])
        val timestamp = asString(map["timestamp"])
        if (!success) {
            return SheetsFullPullResult(
                success = false,
                spreadsheetUrl = spreadsheetUrl,
                message = asString(map["error"]).ifBlank { "Gagal menarik data lengkap dari Google Sheets." }
            )
        }

        val parsedJackets = parsePullResponse(body).jackets

        val rawProds = map["productions"] as? List<*> ?: emptyList<Any?>()
        val parsedProds = rawProds.mapIndexedNotNull { index, raw ->
            val obj = raw as? Map<*, *> ?: return@mapIndexedNotNull null
            val id = asString(obj["id"]).trim().ifBlank { "prod_sheet_$index" }
            val shiftRaw = asString(obj["shift"]).trim().uppercase()
            val shift = when {
                shiftRaw.contains("NIGHT") || shiftRaw.contains("MALAM") -> ProductionShift.NIGHT
                shiftRaw.contains("AFTERNOON") || shiftRaw.contains("SIANG") || shiftRaw.contains("SORE") -> ProductionShift.AFTERNOON
                else -> ProductionShift.MORNING
            }
            val statusRaw = asString(obj["status"]).trim().uppercase()
            val status = when {
                statusRaw.contains("PROGRESS") || statusRaw.contains("PROSES") -> ProductionStatus.IN_PROGRESS
                statusRaw.contains("QC") || statusRaw.contains("QUALITY") || statusRaw.contains("CHECK") -> ProductionStatus.QUALITY_CHECK
                else -> ProductionStatus.COMPLETED
            }
            val nowMs = System.currentTimeMillis()
            val dateVal = asLong(obj["date"], nowMs)
            DailyProductionItem(
                id = id,
                date = dateVal,
                dateString = asString(obj["dateString"]).ifBlank { "2026-01-01" },
                jacketId = asString(obj["jacketId"]),
                jacketName = asString(obj["jacketName"]).ifBlank { "Jaket" },
                jacketSku = asString(obj["jacketSku"]),
                batchNumber = asString(obj["batchNumber"]).ifBlank { "BATCH-${index + 1}" },
                shift = shift,
                operatorName = asString(obj["operatorName"]).ifBlank { "Operator" },
                quantityProduced = asInt(obj["quantityProduced"], 0).coerceAtLeast(0),
                defectCount = asInt(obj["defectCount"], 0).coerceAtLeast(0),
                status = status,
                notes = asString(obj["notes"]),
                addedToStock = asBoolean(obj["addedToStock"], true),
                createdAt = asLong(obj["createdAt"], dateVal)
            )
        }

        val rawKasbon = (map["kasbon"] as? List<*>) ?: (map["data"] as? List<*>) ?: emptyList<Any?>()
        val parsedKasbon = rawKasbon.mapIndexedNotNull { index, raw ->
            val obj = raw as? Map<*, *> ?: return@mapIndexedNotNull null
            val employeeName = asString(obj["employeeName"]).trim()
            val id = asString(obj["id"]).trim().ifBlank { "ksb_sheet_$index" }
            if (employeeName.isBlank()) return@mapIndexedNotNull null

            val amount = CurrencyUtils.normalizeRupiah(asDouble(obj["amount"], 0.0))
            val paid = asDouble(obj["paidAmount"], 0.0).coerceIn(0.0, amount)
            val statusRaw = asString(obj["status"]).trim().uppercase()
            val status = when {
                statusRaw == "LUNAS" || (amount > 0 && paid >= amount) -> KasbonStatus.LUNAS
                statusRaw == "CICILAN" || paid > 0.0 -> KasbonStatus.CICILAN
                else -> KasbonStatus.BELUM_LUNAS
            }
            val nowMs = System.currentTimeMillis()
            val dateVal = asLong(obj["date"], nowMs)
            KasbonItem(
                id = id,
                employeeName = employeeName,
                role = asString(obj["role"]).ifBlank { asString(obj["employeeRole"]).ifBlank { "Operator Jahit" } },
                amount = amount,
                paidAmount = paid,
                date = dateVal,
                dateString = asString(obj["dateString"]).ifBlank { asString(obj["dateIssued"]).ifBlank { "2026-01-01" } },
                dueDate = asString(obj["dueDate"]).ifBlank { "Akhir Bulan" },
                status = status,
                purpose = asString(obj["purpose"]).ifBlank { "Keperluan Pribadi" },
                repaymentMethod = asString(obj["repaymentMethod"]).ifBlank { "Potong Gaji Bulanan" },
                notes = asString(obj["notes"]),
                createdAt = dateVal
            )
        }

        val message = asString(map["message"]).ifBlank {
            "Sinkronisasi tarik selesai: ${parsedJackets.size} jaket, ${parsedProds.size} produksi, ${parsedKasbon.size} kasbon."
        }
        return SheetsFullPullResult(
            success = true,
            jackets = parsedJackets,
            productions = parsedProds,
            kasbon = parsedKasbon,
            spreadsheetUrl = spreadsheetUrl,
            timestamp = timestamp,
            message = message
        )
    }

    suspend fun testConnection(webAppUrl: String): SheetsPingResult = withContext(Dispatchers.IO) {
        if (!isValidAppsScriptUrl(webAppUrl)) {
            return@withContext SheetsPingResult(
                success = false,
                message = "URL tidak valid. Gunakan URL Web App Google Apps Script (https://script.google.com/macros/s/.../exec)."
            )
        }
        try {
            val request = Request.Builder()
                .url(buildActionUrl(webAppUrl, "ping"))
                .get()
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful && body.isBlank()) {
                    return@withContext SheetsPingResult(
                        success = false,
                        message = "Server mengembalikan HTTP ${response.code}."
                    )
                }
                parsePingResponse(body)
            }
        } catch (e: IOException) {
            SheetsPingResult(
                success = false,
                message = "Gagal menghubungi Google Apps Script: ${e.localizedMessage ?: "Periksa koneksi internet."}"
            )
        } catch (e: Exception) {
            SheetsPingResult(
                success = false,
                message = "Terjadi kesalahan koneksi: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    suspend fun pushInventory(webAppUrl: String, jackets: List<JacketItem>): SheetsPushResult =
        withContext(Dispatchers.IO) {
            if (!isValidAppsScriptUrl(webAppUrl)) {
                return@withContext SheetsPushResult(
                    success = false,
                    message = "URL Web App tidak valid. Pastikan berakhiran /exec."
                )
            }
            if (jackets.isEmpty()) {
                return@withContext SheetsPushResult(
                    success = false,
                    message = "Tidak ada data jaket untuk dikirim."
                )
            }
            try {
                val jsonBody = buildSyncPayloadJson(jackets)
                val request = Request.Builder()
                    .url(webAppUrl.trim())
                    .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                    .header("Accept", "application/json")
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful && body.isBlank()) {
                        return@withContext SheetsPushResult(
                            success = false,
                            message = "Gagal mengirim data (HTTP ${response.code})."
                        )
                    }
                    parsePushResponse(body)
                }
            } catch (e: IOException) {
                SheetsPushResult(
                    success = false,
                    message = "Gagal terhubung saat mengirim data: ${e.localizedMessage ?: "Periksa internet."}"
                )
            } catch (e: Exception) {
                SheetsPushResult(
                    success = false,
                    message = "Kesalahan saat push ke Sheets: ${e.localizedMessage ?: e.javaClass.simpleName}"
                )
            }
        }

    suspend fun pullInventory(webAppUrl: String): SheetsPullResult = withContext(Dispatchers.IO) {
        if (!isValidAppsScriptUrl(webAppUrl)) {
            return@withContext SheetsPullResult(
                success = false,
                message = "URL Web App tidak valid. Pastikan berakhiran /exec."
            )
        }
        try {
            val request = Request.Builder()
                .url(buildActionUrl(webAppUrl, "getInventory"))
                .get()
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful && body.isBlank()) {
                    return@withContext SheetsPullResult(
                        success = false,
                        message = "Gagal mengambil data (HTTP ${response.code})."
                    )
                }
                parsePullResponse(body)
            }
        } catch (e: IOException) {
            SheetsPullResult(
                success = false,
                message = "Gagal terhubung saat menarik data: ${e.localizedMessage ?: "Periksa internet."}"
            )
        } catch (e: Exception) {
            SheetsPullResult(
                success = false,
                message = "Kesalahan saat pull dari Sheets: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    suspend fun repairSpreadsheet(webAppUrl: String): SheetsRepairResult = withContext(Dispatchers.IO) {
        if (!isValidAppsScriptUrl(webAppUrl)) {
            return@withContext SheetsRepairResult(
                success = false,
                message = "URL Web App tidak valid. Pastikan berakhiran /exec."
            )
        }
        try {
            val request = Request.Builder()
                .url(webAppUrl.trim())
                .post(buildRepairPayloadJson().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful && body.isBlank()) {
                    return@withContext SheetsRepairResult(
                        success = false,
                        message = "Gagal memperbaiki spreadsheet (HTTP ${response.code})."
                    )
                }
                parseRepairResponse(body)
            }
        } catch (e: IOException) {
            SheetsRepairResult(
                success = false,
                message = "Gagal terhubung saat memperbaiki spreadsheet: ${e.localizedMessage ?: "Periksa internet."}"
            )
        } catch (e: Exception) {
            SheetsRepairResult(
                success = false,
                message = "Kesalahan saat perbaikan spreadsheet: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    suspend fun pushAllData(
        webAppUrl: String,
        jackets: List<JacketItem>,
        productions: List<DailyProductionItem>,
        kasbonList: List<KasbonItem>,
        replaceMissing: Boolean = true
    ): SheetsFullPushResult = withContext(Dispatchers.IO) {
        if (!isValidAppsScriptUrl(webAppUrl)) {
            return@withContext SheetsFullPushResult(
                success = false,
                message = "URL Web App tidak valid. Pastikan berakhiran /exec."
            )
        }
        try {
            val jsonBody = buildFullSyncPayloadJson(jackets, productions, kasbonList, replaceMissing)
            val request = Request.Builder()
                .url(webAppUrl.trim())
                .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful && body.isBlank()) {
                    return@withContext SheetsFullPushResult(
                        success = false,
                        message = "Gagal sinkronisasi penuh (HTTP ${response.code})."
                    )
                }
                parseFullPushResponse(body)
            }
        } catch (e: IOException) {
            SheetsFullPushResult(
                success = false,
                message = "Gagal terhubung saat sinkronisasi penuh: ${e.localizedMessage ?: "Periksa internet."}"
            )
        } catch (e: Exception) {
            SheetsFullPushResult(
                success = false,
                message = "Kesalahan sinkronisasi penuh: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    suspend fun pullAllData(webAppUrl: String): SheetsFullPullResult = withContext(Dispatchers.IO) {
        if (!isValidAppsScriptUrl(webAppUrl)) {
            return@withContext SheetsFullPullResult(
                success = false,
                message = "URL Web App tidak valid. Pastikan berakhiran /exec."
            )
        }
        try {
            val request = Request.Builder()
                .url(buildActionUrl(webAppUrl, "getAllData"))
                .get()
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful && body.isBlank()) {
                    return@withContext SheetsFullPullResult(
                        success = false,
                        message = "Gagal menarik seluruh data (HTTP ${response.code})."
                    )
                }
                val parsed = parseFullPullResponse(body)
                if (parsed.success) {
                    parsed
                } else {
                    // Fallback ke endpoint lama getInventory jika Code.gs belum di-deploy ulang ke v2.3
                    val legacy = pullInventory(webAppUrl)
                    if (legacy.success) {
                        SheetsFullPullResult(
                            success = true,
                            jackets = legacy.jackets,
                            spreadsheetUrl = legacy.spreadsheetUrl,
                            message = legacy.message
                        )
                    } else {
                        parsed
                    }
                }
            }
        } catch (e: IOException) {
            SheetsFullPullResult(
                success = false,
                message = "Gagal terhubung saat menarik seluruh data: ${e.localizedMessage ?: "Periksa internet."}"
            )
        } catch (e: Exception) {
            SheetsFullPullResult(
                success = false,
                message = "Kesalahan saat menarik seluruh data: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    private fun parseJsonObject(body: String): Map<String, Any?>? {
        return try {
            mapAdapter.fromJson(body)
        } catch (_: Exception) {
            null
        }
    }

    private fun asString(value: Any?): String = when (value) {
        null -> ""
        is String -> value
        is Double -> if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
        else -> value.toString()
    }

    private fun asInt(value: Any?, default: Int): Int = when (value) {
        is Number -> value.toInt()
        is String -> value.trim().toDoubleOrNull()?.toInt() ?: default
        else -> default
    }

    private fun asLong(value: Any?, default: Long): Long = when (value) {
        is Number -> value.toLong()
        is String -> value.trim().toDoubleOrNull()?.toLong() ?: default
        else -> default
    }

    private fun asDouble(value: Any?, default: Double): Double = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().replace("[^0-9.,-]".toRegex(), "").replace(",", "").toDoubleOrNull() ?: default
        else -> default
    }

    private fun asBoolean(value: Any?, default: Boolean): Boolean = when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> when (value.trim().lowercase()) {
            "true", "ya", "yes", "1" -> true
            "false", "tidak", "no", "0" -> false
            else -> default
        }
        else -> default
    }
}

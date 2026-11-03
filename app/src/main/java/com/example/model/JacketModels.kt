package com.example.model

enum class StockStatusLevel {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class JacketItem(
    val id: String,
    val sku: String,
    val name: String,
    val style: String,
    val size: String,
    val material: String,
    val color: String,
    val quantity: Int,
    val minThreshold: Int = 5,
    val locationRack: String,
    val imageUrl: String,
    val isSeasonal: Boolean = false,
    val unitPrice: Double = 89.99,
    val genderCategory: String = "Jaket Pria"
) {
    val status: StockStatusLevel
        get() = when {
            quantity <= 0 -> StockStatusLevel.OUT_OF_STOCK
            quantity <= minThreshold -> StockStatusLevel.LOW_STOCK
            else -> StockStatusLevel.IN_STOCK
        }
}

data class StyleCategory(
    val id: String,
    val titleEn: String,
    val titleId: String,
    val descEn: String,
    val descId: String,
    val imageUrl: String
)

data class SizeCategory(
    val id: String,
    val title: String,
    val descEn: String,
    val descId: String,
    val imageUrl: String
)

data class StockStatusCategory(
    val id: String,
    val status: StockStatusLevel,
    val titleEn: String,
    val titleId: String,
    val descEn: String,
    val descId: String,
    val imageUrl: String
)

data class MaterialCategory(
    val id: String,
    val titleEn: String,
    val titleId: String,
    val descEn: String,
    val descId: String,
    val imageUrl: String
)

data class ColorCategory(
    val id: String,
    val titleEn: String,
    val titleId: String,
    val descEn: String,
    val descId: String,
    val hexColor: Long,
    val imageUrl: String
)

data class BannerData(
    val titleEn: String,
    val titleId: String,
    val subtitleEn: String,
    val subtitleId: String,
    val imageUrl: String
)

data class DemographicCategory(
    val id: String,
    val titleId: String,
    val titleEn: String,
    val descId: String,
    val descEn: String,
    val tag: String,
    val imageUrl: String
)

sealed class ActiveFilter {
    object None : ActiveFilter()
    data class ByStyle(val style: String, val title: String) : ActiveFilter()
    data class BySize(val size: String, val title: String) : ActiveFilter()
    data class ByStatus(val status: StockStatusLevel, val title: String) : ActiveFilter()
    data class ByMaterial(val material: String, val title: String) : ActiveFilter()
    data class ByColor(val color: String, val title: String) : ActiveFilter()
    data class BySeasonal(val title: String) : ActiveFilter()
    data class ByCategory(val category: String, val title: String) : ActiveFilter()
}

object CatalogData {
    const val HERO_IMAGE_URL = "https://mediadirhub.com/vibe/industrial-warehouse-with-stacked-jackets-on-metal-racks-under-fluorescent-lighting.png"

    val GENDER_CATEGORIES = listOf(
        DemographicCategory(
            id = "cat_pria",
            titleId = "Jaket Pria",
            titleEn = "Men's Jackets",
            descId = "Koleksi jaket maskulin untuk pria dewasa",
            descEn = "Masculine outerwear collection for men",
            tag = "Pria",
            imageUrl = "https://mediadirhub.com/vibe/solid-black-jacket-front-with-minimal-branding.png"
        ),
        DemographicCategory(
            id = "cat_wanita",
            titleId = "Jaket Wanita",
            titleEn = "Women's Jackets",
            descId = "Koleksi jaket feminin, modis dan elegan",
            descEn = "Feminine, stylish and chic outerwear",
            tag = "Wanita",
            imageUrl = "https://mediadirhub.com/vibe/light-beige-jacket-with-soft-texture-and-neutral-tone.png"
        ),
        DemographicCategory(
            id = "cat_anak",
            titleId = "Jaket Anak",
            titleEn = "Kids' Jackets",
            descId = "Jaket hangat, nyaman dan lucu untuk anak-anak",
            descEn = "Warm, comfortable and fun jackets for kids",
            tag = "Anak",
            imageUrl = "https://mediadirhub.com/vibe/medium-blue-denim-jacket-with-patch-pockets-and-button-front.png"
        ),
        DemographicCategory(
            id = "cat_unisex",
            titleId = "Jaket Unisex",
            titleEn = "Unisex Jackets",
            descId = "Desain fleksibel cocok untuk semua kalangan",
            descEn = "Versatile design suitable for all genders",
            tag = "Unisex",
            imageUrl = "https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png"
        )
    )

    val STYLES = listOf(
        StyleCategory(
            id = "style_bomber",
            titleEn = "Bomber Jacket",
            titleId = "Bomber Jacket",
            descEn = "Classic military-inspired outerwear",
            descId = "Pakaian luar klasik bernuansa militer",
            imageUrl = "https://mediadirhub.com/vibe/black-leather-bomber-jacket-with-ribbed-cuffs-and-zipper-front.png"
        ),
        StyleCategory(
            id = "style_trench",
            titleEn = "Trench Coat",
            titleId = "Trench Coat",
            descEn = "Water-resistant double-breasted design",
            descId = "Desain ganda dengan tahan air",
            imageUrl = "https://mediadirhub.com/vibe/beige-cotton-trench-coat-with-double-breasted-buttons-and-belt.png"
        ),
        StyleCategory(
            id = "style_denim",
            titleEn = "Denim Jacket",
            titleId = "Denim Jacket",
            descEn = "Rugged cotton denim with button front",
            descId = "Denim katun kasar dengan kancing depan",
            imageUrl = "https://mediadirhub.com/vibe/medium-blue-denim-jacket-with-patch-pockets-and-button-front.png"
        ),
        StyleCategory(
            id = "style_puffer",
            titleEn = "Puffer Jacket",
            titleId = "Puffer Jacket",
            descEn = "Lightweight insulated with quilted fabric",
            descId = "Insulasi ringan dengan kain berlapis",
            imageUrl = "https://mediadirhub.com/vibe/dark-gray-quilted-puffer-jacket-with-hood-and-zip-closure.png"
        )
    )

    val SIZES = listOf(
        SizeCategory(
            id = "size_xs",
            title = "XS",
            descEn = "Fits chest 76–81 cm",
            descId = "Cocok untuk dada 76–81 cm",
            imageUrl = "https://mediadirhub.com/vibe/white-label-with-black-text-xs-on-a-gray-fabric-swatch.png"
        ),
        SizeCategory(
            id = "size_s",
            title = "S",
            descEn = "Fits chest 81–86 cm",
            descId = "Cocok untuk dada 81–86 cm",
            imageUrl = "https://mediadirhub.com/vibe/white-label-with-black-text-s-on-a-gray-fabric-swatch.png"
        ),
        SizeCategory(
            id = "size_m",
            title = "M",
            descEn = "Fits chest 86–91 cm",
            descId = "Cocok untuk dada 86–91 cm",
            imageUrl = "https://mediadirhub.com/vibe/white-label-with-black-text-m-on-a-gray-fabric-swatch.png"
        ),
        SizeCategory(
            id = "size_l",
            title = "L",
            descEn = "Fits chest 91–96 cm",
            descId = "Cocok untuk dada 91–96 cm",
            imageUrl = "https://mediadirhub.com/vibe/white-label-with-black-text-l-on-a-gray-fabric-swatch.png"
        ),
        SizeCategory(
            id = "size_xl",
            title = "XL",
            descEn = "Fits chest 96–101 cm",
            descId = "Cocok untuk dada 96–101 cm",
            imageUrl = "https://mediadirhub.com/vibe/white-label-with-black-text-xl-on-a-gray-fabric-swatch.png"
        ),
        SizeCategory(
            id = "size_xxl",
            title = "XXL",
            descEn = "Fits chest 101–106 cm",
            descId = "Cocok untuk dada 101–106 cm",
            imageUrl = "https://mediadirhub.com/vibe/white-label-with-black-text-xxl-on-a-gray-fabric-swatch.png"
        ),
        SizeCategory(
            id = "size_3xl",
            title = "3XL",
            descEn = "Fits chest 106–112 cm",
            descId = "Cocok untuk dada 106–112 cm",
            imageUrl = "https://mediadirhub.com/vibe/white-label-with-black-text-xl-on-a-gray-fabric-swatch.png"
        )
    )

    val STOCK_STATUSES = listOf(
        StockStatusCategory(
            id = "status_in",
            status = StockStatusLevel.IN_STOCK,
            titleEn = "In Stock",
            titleId = "Stok Tersedia",
            descEn = "Ready for immediate dispatch",
            descId = "Siap untuk pengiriman segera",
            imageUrl = "https://mediadirhub.com/vibe/green-checkmark-icon-on-white-background-with-jacket-silhouette.png"
        ),
        StockStatusCategory(
            id = "status_low",
            status = StockStatusLevel.LOW_STOCK,
            titleEn = "Low Stock",
            titleId = "Stok Sedikit",
            descEn = "Few units remaining, restock needed",
            descId = "Sisa unit sedikit, perlu restock",
            imageUrl = "https://mediadirhub.com/vibe/yellow-exclamation-mark-icon-on-white-background-with-jacket-silhouette.png"
        ),
        StockStatusCategory(
            id = "status_out",
            status = StockStatusLevel.OUT_OF_STOCK,
            titleEn = "Out of Stock",
            titleId = "Habis Stok",
            descEn = "Currently unavailable, pending restock",
            descId = "Tidak tersedia saat ini, menunggu restock",
            imageUrl = "https://mediadirhub.com/vibe/red-x-icon-on-white-background-with-jacket-silhouette.png"
        )
    )

    val SEASONAL_BANNER = BannerData(
        titleEn = "Seasonal Collection",
        titleId = "Koleksi Musiman",
        subtitleEn = "Winter collection jackets with thermal lining",
        subtitleId = "Jaket musim dingin dengan lapisan termal",
        imageUrl = "https://mediadirhub.com/vibe/winter-themed-display-of-puffer-and-wool-jackets-on-wooden-hangers.png"
    )

    val MATERIALS = listOf(
        MaterialCategory(
            id = "material_cotton",
            titleEn = "Cotton",
            titleId = "Katun",
            descEn = "Natural breathable cotton fabric",
            descId = "Kain katun alami yang bernapas",
            imageUrl = "https://mediadirhub.com/vibe/close-up-of-woven-cotton-fabric-with-visible-threads.png"
        ),
        MaterialCategory(
            id = "material_polyester",
            titleEn = "Polyester",
            titleId = "Polyster",
            descEn = "Durable synthetic fiber blend",
            descId = "Campuran serat sintetis yang tahan lama",
            imageUrl = "https://mediadirhub.com/vibe/close-up-of-smooth-synthetic-polyester-fabric-with-slight-sheen.png"
        ),
        MaterialCategory(
            id = "material_wool",
            titleEn = "Wool",
            titleId = "Wol",
            descEn = "Warm insulating natural fiber",
            descId = "Serat alami yang hangat dan menghangatkan",
            imageUrl = "https://mediadirhub.com/vibe/close-up-of-thick-wool-knit-with-visible-fiber-texture.png"
        ),
        MaterialCategory(
            id = "material_nylon",
            titleEn = "Nylon",
            titleId = "Nilon",
            descEn = "Lightweight water-resistant fabric",
            descId = "Kain ringan yang tahan air",
            imageUrl = "https://mediadirhub.com/vibe/close-up-of-tightly-woven-nylon-fabric-with-water-repellent-finish.png"
        ),
        MaterialCategory(
            id = "material_leather",
            titleEn = "Leather",
            titleId = "Kulit",
            descEn = "Premium animal hide with smooth finish",
            descId = "Tampilan premium dari kulit hewan",
            imageUrl = "https://mediadirhub.com/vibe/close-up-of-smooth-brown-leather-with-natural-grain-pattern.png"
        ),
        MaterialCategory(
            id = "material_fleece",
            titleEn = "Fleece",
            titleId = "Fleece",
            descEn = "Soft thermal synthetic pile fabric",
            descId = "Kain sintetis lembut dengan permukaan berbulu",
            imageUrl = "https://mediadirhub.com/vibe/close-up-of-soft-synthetic-fleece-with-fluffy-pile-surface.png"
        )
    )

    val COLORS = listOf(
        ColorCategory(
            id = "color_pink",
            titleEn = "Pink",
            titleId = "Pink",
            descEn = "Soft rose blush outerwear",
            descId = "Nuansa merah muda cerah dan modis",
            hexColor = 0xFFF472B6,
            imageUrl = "https://mediadirhub.com/vibe/light-beige-jacket-with-soft-texture-and-neutral-tone.png"
        ),
        ColorCategory(
            id = "color_beige",
            titleEn = "Beige",
            titleId = "Beige",
            descEn = "Sophisticated neutral warm tone",
            descId = "Nuansa krem netral yang mewah",
            hexColor = 0xFFE5D5C5,
            imageUrl = "https://mediadirhub.com/vibe/light-beige-jacket-with-soft-texture-and-neutral-tone.png"
        ),
        ColorCategory(
            id = "color_maroon",
            titleEn = "Maroon",
            titleId = "Maroon",
            descEn = "Rich deep wine red shade",
            descId = "Warna merah marun pekat dan berkelas",
            hexColor = 0xFF800020,
            imageUrl = "https://mediadirhub.com/vibe/deep-navy-blue-jacket-with-clean-lines-and-no-patterns.png"
        ),
        ColorCategory(
            id = "color_merah",
            titleEn = "Red",
            titleId = "Merah",
            descEn = "Bold energetic crimson hue",
            descId = "Warna merah menyala penuh percaya diri",
            hexColor = 0xFFDC2626,
            imageUrl = "https://mediadirhub.com/vibe/red-x-icon-on-white-background-with-jacket-silhouette.png"
        ),
        ColorCategory(
            id = "color_abu",
            titleEn = "Gray",
            titleId = "Abu",
            descEn = "Sleek heather gray finish",
            descId = "Nuansa abu-abu netral minimalis",
            hexColor = 0xFF6B7280,
            imageUrl = "https://mediadirhub.com/vibe/dark-gray-quilted-puffer-jacket-with-hood-and-zip-closure.png"
        ),
        ColorCategory(
            id = "color_hijau",
            titleEn = "Green",
            titleId = "Hijau",
            descEn = "Vibrant emerald and forest green",
            descId = "Warna hijau alami yang segar dan tegas",
            hexColor = 0xFF15803D,
            imageUrl = "https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png"
        ),
        ColorCategory(
            id = "color_coklat",
            titleEn = "Brown",
            titleId = "Coklat",
            descEn = "Warm rustic leather brown",
            descId = "Warna cokelat klasik yang hangat",
            hexColor = 0xFF78350F,
            imageUrl = "https://mediadirhub.com/vibe/close-up-of-smooth-brown-leather-with-natural-grain-pattern.png"
        ),
        ColorCategory(
            id = "color_caramel",
            titleEn = "Caramel",
            titleId = "Caramel",
            descEn = "Warm golden toffee amber",
            descId = "Sentuhan warna karamel keemasan",
            hexColor = 0xFFC27835,
            imageUrl = "https://mediadirhub.com/vibe/beige-cotton-trench-coat-with-double-breasted-buttons-and-belt.png"
        ),
        ColorCategory(
            id = "color_putih",
            titleEn = "White",
            titleId = "Putih",
            descEn = "Clean minimalist snow white",
            descId = "Warna putih bersih dan modern",
            hexColor = 0xFFFFFFFF,
            imageUrl = "https://mediadirhub.com/vibe/solid-black-jacket-front-with-minimal-branding.png"
        ),
        ColorCategory(
            id = "color_black",
            titleEn = "Black",
            titleId = "Hitam",
            descEn = "Classic matte finish",
            descId = "Finishing matte hitam klasik",
            hexColor = 0xFF18181B,
            imageUrl = "https://mediadirhub.com/vibe/solid-black-jacket-front-with-minimal-branding.png"
        ),
        ColorCategory(
            id = "color_navy",
            titleEn = "Navy",
            titleId = "Navy",
            descEn = "Deep indigo tone",
            descId = "Nada indigo yang dalam",
            hexColor = 0xFF1E3A8A,
            imageUrl = "https://mediadirhub.com/vibe/deep-navy-blue-jacket-with-clean-lines-and-no-patterns.png"
        ),
        ColorCategory(
            id = "color_olive",
            titleEn = "Olive",
            titleId = "Olive",
            descEn = "Earthy green-brown hue",
            descId = "Warna hijau-coklat kebumian",
            hexColor = 0xFF3F4F2A,
            imageUrl = "https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png"
        )
    )
}

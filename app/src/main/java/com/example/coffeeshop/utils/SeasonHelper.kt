package com.example.coffeeshop.utils

import java.util.*

/**
 * Enum định nghĩa các mùa trong năm
 */
enum class Season {
    SPRING,  // Xuân: Tháng 3, 4, 5
    SUMMER,  // Hè: Tháng 6, 7, 8
    FALL,    // Thu: Tháng 9, 10, 11
    WINTER   // Đông: Tháng 12, 1, 2
}

/**
 * SeasonHelper - Quản lý thông tin về mùa
 * Dùng để gợi ý sản phẩm theo mùa
 */
object SeasonHelper {
    // Biến để override mùa khi test (set null để dùng mùa thật)
    private var testSeason: Season? = null

    /**
     * Xác định mùa hiện tại dựa trên tháng
     */
    fun getCurrentSeason(): Season {
        // Nếu đang test, trả về mùa test
        testSeason?.let { return it }

        val month = Calendar.getInstance().get(Calendar.MONTH) + 1 // Tháng bắt đầu từ 0

        return when (month) {
            in 3..5 -> Season.SPRING   // Xuân
            in 6..8 -> Season.SUMMER   // Hè
            in 9..11 -> Season.FALL     // Thu
            else -> Season.WINTER       // Đông (12, 1, 2)
        }
    }

    /**
     * Set mùa test (dùng để test các mùa khác nhau)
     * Set null để quay lại dùng mùa thật
     */
    fun setTestSeason(season: Season?) {
        testSeason = season
    }

    /**
     * Kiểm tra xem đang ở chế độ test không
     */
    fun isTestMode(): Boolean {
        return testSeason != null
    }

    /**
     * Lấy danh sách từ khóa tìm kiếm cho mùa
     * Dùng để tìm sản phẩm phù hợp
     */
    fun getSeasonKeywords(season: Season): List<String> {
        return when (season) {
            Season.SPRING -> listOf(
                "trà", "tea", "matcha", "hoa", "spring",
                "tươi", "mát", "nhẹ"
            )
            Season.SUMMER -> listOf(
                "trà chanh", "lemonade", "đá xay", "iced", "lạnh",
                "cold", "summer", "mát mẻ", "sinh tố"
            )
            Season.FALL -> listOf(
                "pumpkin", "caramel", "cinnamon", "fall", "autumn",
                "bí đỏ", "mềm mại", "ấm"
            )
            Season.WINTER -> listOf(
                "cacao", "hot chocolate", "nóng", "hot", "winter",
                "warm", "ấm áp", "coffee"
            )
        }
    }

    /**
     * Lấy tên mùa bằng tiếng Việt
     */
    fun getSeasonName(season: Season): String {
        return when (season) {
            Season.SPRING -> "Xuân"
            Season.SUMMER -> "Hè"
            Season.FALL -> "Thu"
            Season.WINTER -> "Đông"
        }
    }

    /**
     * Lấy mô tả mùa
     */
    fun getSeasonDescription(season: Season): String {
        return when (season) {
            Season.SPRING -> "Mùa xuân tươi mát"
            Season.SUMMER -> "Mùa hè nắng nóng"
            Season.FALL -> "Mùa thu se lạnh"
            Season.WINTER -> "Mùa đông lạnh lẽo"
        }
    }

    /**
     * Lấy emoji đại diện cho mùa
     */
    fun getSeasonEmoji(season: Season): String {
        return when (season) {
            Season.SPRING -> "🌸"
            Season.SUMMER -> "☀️"
            Season.FALL -> "🍂"
            Season.WINTER -> "❄️"
        }
    }
}
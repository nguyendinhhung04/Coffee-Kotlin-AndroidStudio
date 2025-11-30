package com.example.coffeeshop.utils

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

enum class WeatherType {
    SUNNY,      // Nắng nóng
    RAINY,      // Mưa
    COLD,       // Lạnh
    CLOUDY,     // Nhiều mây
    WINDY       // Gió
}

object WeatherHelper {
    private val client = OkHttpClient()
    // API key miễn phí từ OpenWeatherMap (bạn cần đăng ký tại openweathermap.org)
    private const val API_KEY = "YOUR_API_KEY_HERE" // Thay bằng API key của bạn
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"
    
    // Biến để lưu thời tiết hiện tại (cache)
    private var currentWeather: WeatherType? = null
    private var cachedCity: String? = null

    /**
     * Lấy thời tiết hiện tại dựa trên thành phố
     * @param city Tên thành phố (ví dụ: "Ho Chi Minh", "Hanoi")
     * @param callback Callback trả về WeatherType
     */
    fun getCurrentWeather(city: String = "Ho Chi Minh", callback: (WeatherType?, String) -> Unit) {
        // Nếu đã cache và cùng thành phố, trả về cache
        if (currentWeather != null && cachedCity == city) {
            callback(currentWeather, "Thời tiết từ cache")
            return
        }

        val url = "$BASE_URL?q=$city&appid=$API_KEY&units=metric&lang=vi"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, "Lỗi kết nối: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val json = JSONObject(body)
                        val weatherType = parseWeather(json)
                        currentWeather = weatherType
                        cachedCity = city
                        callback(weatherType, "Thời tiết: ${getWeatherName(weatherType)}")
                    } catch (e: Exception) {
                        callback(null, "Lỗi parse: ${e.message}")
                    }
                } else {
                    callback(null, "Lỗi API: ${response.code}")
                }
            }
        })
    }

    /**
     * Parse dữ liệu thời tiết từ JSON
     */
    private fun parseWeather(json: JSONObject): WeatherType {
        val weatherArray = json.getJSONArray("weather")
        val weatherMain = weatherArray.getJSONObject(0).getString("main").lowercase()
        val temp = json.getJSONObject("main").getDouble("temp")
        
        return when {
            weatherMain.contains("rain") || weatherMain.contains("drizzle") -> WeatherType.RAINY
            temp < 20 -> WeatherType.COLD
            temp > 30 && weatherMain.contains("clear") -> WeatherType.SUNNY
            weatherMain.contains("cloud") -> WeatherType.CLOUDY
            weatherMain.contains("wind") -> WeatherType.WINDY
            else -> WeatherType.SUNNY // Mặc định
        }
    }

    /**
     * Lấy từ khóa tìm kiếm món dựa trên thời tiết
     */
    fun getWeatherKeywords(weather: WeatherType): List<String> {
        return when (weather) {
            WeatherType.SUNNY -> listOf("trà chanh", "lemonade", "đá xay", "iced", "lạnh", "cold", "nước ép")
            WeatherType.RAINY -> listOf("cacao", "hot chocolate", "nóng", "hot", "warm", "trà nóng", "coffee")
            WeatherType.COLD -> listOf("cacao", "hot chocolate", "nóng", "hot", "warm", "trà nóng", "soup")
            WeatherType.CLOUDY -> listOf("coffee", "latte", "cappuccino", "trà", "tea")
            WeatherType.WINDY -> listOf("warm", "nóng", "hot", "coffee", "trà nóng")
        }
    }

    /**
     * Lấy tên thời tiết bằng tiếng Việt
     */
    fun getWeatherName(weather: WeatherType): String {
        return when (weather) {
            WeatherType.SUNNY -> "Nắng nóng"
            WeatherType.RAINY -> "Mưa"
            WeatherType.COLD -> "Lạnh"
            WeatherType.CLOUDY -> "Nhiều mây"
            WeatherType.WINDY -> "Gió"
        }
    }

    /**
     * Set thời tiết test (không cần API)
     */
    fun setTestWeather(weather: WeatherType?) {
        currentWeather = weather
    }

    /**
     * Lấy thời tiết hiện tại (có thể là test hoặc thật)
     */
    fun getCurrentWeatherType(): WeatherType? {
        return currentWeather
    }
}


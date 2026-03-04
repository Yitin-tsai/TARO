# 🍠 TARO — Taipei Area Realtime Overview

> 打開的瞬間，不用問任何問題，就看到你現在最需要知道的一切。

TARO 是一個基於地理位置的台北即時生活資訊整合工具。透過 GPS 定位，一秒內顯示你附近的天氣、空氣品質、YouBike 車位、捷運到站時間、公車到站時間，以及 AI 生成的出門建議。

**[👉 Live Demo](https://taro.pages.dev)** ｜ **[📄 API Docs](https://taro-api.fly.dev/swagger-ui.html)**

---

## ✨ Features

- **零輸入體驗** — 打開即定位，一個畫面看完所有出門資訊
- **即時交通整合** — 捷運到站、公車到站、YouBike 車位，通通即時更新
- **AI 出門建議** — 根據天氣 + 空氣品質 + 紫外線，自動產生穿搭與注意事項
- **智慧對話助手** — 自然語言問路、問天氣，LLM 自動查詢即時資料回覆
- **PWA 支援** — 加到手機主畫面，體驗如原生 App

## 🆚 Why not just ask ChatGPT?

|  | ChatGPT / Claude | TARO |
|--|--|--|
| 操作 | 打字問 → 等回應 | 打開就有，零輸入 |
| 速度 | 5-10 秒 | < 2 秒 |
| 資料 | 網頁搜尋拼湊 | 政府 API 即時精確數據 |
| 定位 | 要自己說在哪 | GPS 自動 |
| 交通 | 一次問一個 | YouBike + 捷運 + 公車一次看完 |

---

## 🏗️ Architecture

```
Cloudflare Pages                    Fly.io
┌──────────────┐     fetch()     ┌──────────────────────────┐
│  Static HTML │ ──────────────▶ │    Spring Boot 3 API     │
│  + CSS + JS  │                 │                          │
│  + PWA       │                 │  ┌────────────────────┐  │
└──────────────┘                 │  │  Caffeine Cache    │  │
                                 │  │  天氣/AQI/YouBike  │  │
                                 │  └────────────────────┘  │
                                 │           +              │
                                 │  ┌────────────────────┐  │
                                 │  │  TDX Realtime      │  │
                                 │  │  捷運/公車到站      │  │
                                 │  └────────────────────┘  │
                                 │           +              │
                                 │  ┌────────────────────┐  │
                                 │  │  Spring AI          │  │
                                 │  │  Function Calling   │  │
                                 │  └────────────────────┘  │
                                 └──────────────────────────┘
                                    │        │        │
                                    ▼        ▼        ▼
                                 環境部   氣象署     TDX
```

### Core API

```
GET /api/dashboard?lat=25.026&lng=121.543
```

One request. Everything you need:

```json
{
  "weather":      { "temp": 27, "rainProb": 30, "desc": "多雲時晴" },
  "airQuality":   { "aqi": 65, "status": "良好", "pm25": 18.3 },
  "advice":       "午後有三成機率下雨，帶把傘...",
  "nearbyYouBike": [{ "name": "大安森林公園", "bikes": 12, "distanceMeters": 150 }],
  "nearbyMetro":  [{ "station": "大安站", "lines": [{ "line": "信義線", "directions": [...] }] }],
  "nearbyBus":    [{ "stopName": "大安森林公園", "routes": [{ "route": "0東", "estimateMin": 3 }] }]
}
```

---

## 🛠️ Tech Stack

### Backend

| | |
|--|--|
| Framework | Spring Boot 3 (Java 17+) |
| HTTP Client | WebClient (async, non-blocking) |
| Cache | Caffeine (in-memory, per-source TTL) |
| Scheduling | @Scheduled (built-in) |
| AI | Spring AI + Anthropic Claude |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Deploy | Docker → Fly.io |

### Frontend

| | |
|--|--|
| Stack | Vanilla HTML + CSS + JavaScript |
| Styling | TailwindCSS (CDN) |
| PWA | manifest.json + Service Worker |
| Deploy | Cloudflare Pages (free) |

### Data Sources

| Data | Source | Auth | Update |
|--|--|--|--|
| Weather | 中央氣象署 Open Data | Free API Key | Every 30 min |
| Air Quality (AQI) | 環境部 Open Data | Free API Key | Every 15 min |
| YouBike | 台北市 Open Data | None needed | Every 2 min |
| Metro Arrivals | TDX | Free API Key | Realtime |
| Bus Arrivals | TDX | Free API Key | Realtime |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Gradle
- API Keys (see below)

### 1. Clone

```bash
git clone https://github.com/YOUR_USERNAME/taro.git
cd taro
```

### 2. Get API Keys

| Platform | URL | Time |
|--|--|--|
| 環境部 | https://data.moenv.gov.tw | Instant |
| 中央氣象署 | https://opendata.cwa.gov.tw | Instant |
| TDX | https://tdx.transportdata.tw | 1-3 days review |
| Anthropic | https://console.anthropic.com | Instant |

### 3. Configure

```bash
cp .env.example .env
# Edit .env with your API keys
```

```properties
MOENV_API_KEY=your-key
CWA_API_KEY=your-key
TDX_CLIENT_ID=your-id
TDX_CLIENT_SECRET=your-secret
ANTHROPIC_API_KEY=your-key
```

### 4. Run Backend

```bash
cd taro-api
./gradlew bootRun
```

API available at `http://localhost:8080`
Swagger UI at `http://localhost:8080/swagger-ui.html`

### 5. Run Frontend (local dev)

```bash
cd taro-web
# Any static server works
npx serve .
# or
python -m http.server 5500
```

Open `http://localhost:5500` on your phone (same Wi-Fi)

---

## 📦 Deploy

### Backend → Fly.io

```bash
cd taro-api
fly launch
fly secrets set MOENV_API_KEY=xxx CWA_API_KEY=xxx ...
fly deploy
```

### Frontend → Cloudflare Pages

1. Push `taro-web/` to GitHub
2. Connect repo in Cloudflare Pages dashboard
3. Set build output directory: `/`
4. Done — auto deploys on every push

---

## 📁 Project Structure

```
taro/
├── taro-api/                       # Spring Boot backend
│   ├── src/main/java/.../
│   │   ├── config/                 # WebClient, Cache, CORS, AI
│   │   ├── client/                 # External API clients
│   │   ├── dto/                    # Data models
│   │   ├── service/                # Business logic + caching
│   │   ├── ai/                     # LLM + Function Calling tools
│   │   └── controller/             # REST endpoints
│   ├── Dockerfile
│   └── build.gradle.kts
│
└── taro-web/                       # Static frontend
    ├── index.html                  # Dashboard
    ├── chat.html                   # Chat with AI assistant
    ├── manifest.json               # PWA config
    ├── css/style.css
    └── js/app.js
```

---

## 🧠 AI Assistant

TARO's chat assistant uses **Spring AI Function Calling** — the LLM can autonomously query realtime data to answer questions:

```
👤 "我要從公館去信義區，今天適合騎 YouBike 嗎？"

🤖 calls getWeather("大安區") → 27°C, 降雨 30%
🤖 calls getNearbyYouBike("公館") → 12 台可借
🤖 calls getTransitRoute("公館", "信義區") → 捷運 15 分

🍠 "今天氣溫 27°C 還不錯，但午後有 30% 機率下雨。
    如果上午出發，公館站有 12 台 YouBike 可借，騎到信義大約 25 分鐘。
    怕下雨的話，搭捷運到市政府站只要 15 分鐘比較保險！"
```

---

## 📄 License

MIT

---

## 🙏 Data Attribution

- 空氣品質資料：[環境部環境資料開放平臺](https://data.moenv.gov.tw)
- 氣象資料：[中央氣象署開放資料平臺](https://opendata.cwa.gov.tw)
- YouBike 資料：[臺北市資料大平臺](https://data.taipei)
- 交通資料：[TDX 運輸資料流通服務平臺](https://tdx.transportdata.tw)

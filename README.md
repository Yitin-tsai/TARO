# TARO — Taipei Area Realtime Overview

> 打開的瞬間，不用問任何問題，就看到你現在最需要知道的一切。

TARO 是一個基於地理位置的大台北即時生活資訊整合 API。透過經緯度座標，一次回傳你附近的天氣、空氣品質、YouBike 車位、捷運到站時間。

---

## Features

### 已實作

- **Dashboard 一次查詢** — 一個 API 回傳天氣 + 空品 + YouBike + 捷運，四個服務平行查詢
- **天氣資訊** — 中央氣象署即時資料，含體感溫度、降雨機率、風速風向、紫外線指數
- **空氣品質** — 環境部 AQI 資料，自動定位最近測站
- **YouBike 車位** — 支援台北市 + 新北市，依距離排序，顯示可借可還數量
- **捷運到站時間** — TDX 靜態時刻表推算下班車時間，支援轉乘站、最近出口資訊
- **智慧快取** — Caffeine in-memory cache，各資料源獨立 TTL（天氣 30min / AQI 15min / YouBike 2min / 捷運站點 24hr）。不使用 `@Scheduled` 定時輪詢，而是透過 `@Cacheable` + TTL 過期機制，在使用者請求時按需刷新，避免無人使用時浪費 API 配額。捷運靜態資料（站點、時刻表、出口）則在啟動時預載入快取

### 預計實作

- **公車到站時間** — TDX 公車即時到站資訊
- **AI 出門建議** — 根據天氣 + 空品 + 紫外線，自動產生穿搭與注意事項
- **智慧對話助手** — 自然語言問路、問天氣，LLM 自動查詢即時資料回覆
- **前端介面** — 手機友善的 Dashboard 頁面 + PWA 支援

---

## Architecture

```
                              Spring Boot 3 API
                           ┌──────────────────────────┐
                           │                          │
  GET /api/dashboard ─────▶│  ┌────────────────────┐  │
                           │  │  Caffeine Cache    │  │
                           │  │  天氣/AQI/YouBike  │  │
                           │  └────────────────────┘  │
                           │           +              │
                           │  ┌────────────────────┐  │
                           │  │  TDX Metro         │  │
                           │  │  時刻表 + 站點快取  │  │
                           │  └────────────────────┘  │
                           └──────────────────────────┘
                              │        │        │
                              ▼        ▼        ▼
                           環境部   氣象署     TDX
                                      +
                                  台北/新北
                                 YouBike API
```

### Core API

```
GET /api/dashboard?lat=25.0478&lng=121.5170
```

一次請求，回傳所有資訊（以台北車站為例）：

```json
{
  "weather": {
    "temp": 18,
    "feelsLike": 18,
    "desc": "陰短暫陣雨",
    "rainProb": 40,
    "humidity": 84,
    "windSpeed": 3.0,
    "uvIndex": 2,
    "district": "中正區"
  },
  "airQuality": {
    "aqi": 43,
    "status": "良好",
    "pm25": 10.0,
    "station": "萬華"
  },
  "metro": {
    "station": "台北車站",
    "distanceMeters": 171.0,
    "nearestExit": { "exitName": "台北車站M1", "distanceMeters": 129.0 },
    "directions": [
      { "lineId": "BL", "lineName": "板南線", "color": "#0070BD", "direction": 0, "trains": [
        { "destination": "南港展覽館", "arrivalTime": "15:26", "waitMin": 2 },
        { "destination": "南港展覽館", "arrivalTime": "15:31", "waitMin": 7 }
      ]},
      { "lineId": "BL", "lineName": "板南線", "color": "#0070BD", "direction": 1, "trains": [
        { "destination": "頂埔", "arrivalTime": "15:24", "waitMin": 0 },
        { "destination": "亞東醫院", "arrivalTime": "15:38", "waitMin": 14 }
      ]},
      { "lineId": "R", "lineName": "淡水信義線", "color": "#E3002C", "direction": 0, "trains": [
        { "destination": "淡水", "arrivalTime": "15:28", "waitMin": 4 },
        { "destination": "北投", "arrivalTime": "15:32", "waitMin": 8 }
      ]},
      { "lineId": "R", "lineName": "淡水信義線", "color": "#E3002C", "direction": 1, "trains": [
        { "destination": "大安", "arrivalTime": "15:24", "waitMin": 0 },
        { "destination": "象山", "arrivalTime": "15:29", "waitMin": 5 }
      ]}
    ]
  },
  "youbike": [
    { "name": "承德鄭州路口(市民高架下)", "availableBikes": 13, "emptySlots": 21, "distanceMeters": 133 },
    { "name": "捷運臺北車站(M2出口)", "availableBikes": 4, "emptySlots": 16, "distanceMeters": 210 },
    { "name": "臺北轉運站(華陰街)", "availableBikes": 15, "emptySlots": 5, "distanceMeters": 277 }
  ]
}
```

### 其他 API

| Endpoint | 說明 |
|---|---|
| `GET /api/weather?lat=&lng=` | 天氣資訊 |
| `GET /api/air-quality?lat=&lng=` | 空氣品質 |
| `GET /api/youbike/nearby?lat=&lng=&radius=` | 附近 YouBike 站點 |
| `GET /api/metro/arrivals?lat=&lng=` | 捷運到站時間 |

---

## Tech Stack

| | |
|---|---|
| Framework | Spring Boot 3.5 (Java 17) |
| HTTP Client | WebClient (non-blocking) |
| Cache | Caffeine (in-memory, per-source TTL) |
| Build | Gradle (Groovy DSL) |
| Container | Docker (multi-stage build) |

### Data Sources

| 資料 | 來源 | 認證 | 快取 TTL |
|---|---|---|---|
| 天氣 | 中央氣象署 Open Data | API Key (免費) | 30 分鐘 |
| 空氣品質 | 環境部 Open Data | API Key (免費) | 15 分鐘 |
| YouBike | 台北市 + 新北市 Open Data | 不需要 | 2 分鐘 |
| 捷運 | TDX 運輸資料平臺 | OAuth2 Client Credentials (免費) | 站點 24hr / 到站即時計算 |

---

## Getting Started

### Prerequisites

- Java 17+
- Docker (optional, for containerized deployment)

### 1. Clone

```bash
git clone https://github.com/Yitin-tsai/TARO.git
cd TARO
```

### 2. Get API Keys

| 平台 | 網址 | 取得時間 |
|---|---|---|
| 中央氣象署 | https://opendata.cwa.gov.tw | 即時 |
| 環境部 | https://data.moenv.gov.tw | 即時 |
| TDX | https://tdx.transportdata.tw | 需審核 1-3 天 |

### 3. Configure

建立 `src/main/resources/application-local.yml`（此檔已加入 .gitignore）：

```yaml
cwa:
  api-key: your-cwa-api-key

moenv:
  api-key: your-moenv-api-key

tdx:
  client-id: your-tdx-client-id
  client-secret: your-tdx-client-secret
```

### 4. Run

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

API available at `http://localhost:8080`

### 5. Run with Docker

```bash
docker build -t taro .

docker run -p 8080:8080 \
  -e CWA_API_KEY=your-key \
  -e MOENV_API_KEY=your-key \
  -e TDX_CLIENT_ID=your-id \
  -e TDX_CLIENT_SECRET=your-secret \
  taro
```

---

## Project Structure

```
taro/
├── src/main/java/com/et/taro/
│   ├── config/          # Properties, WebClient, Cache 設定
│   ├── client/          # 外部 API 客戶端 (CWA, MOENV, TDX, YouBike)
│   ├── dto/             # 資料模型 (含 cwa/, tdx/ 子目錄)
│   ├── service/         # 商業邏輯 + 距離計算 + 快取
│   └── controller/      # REST endpoints
├── src/main/resources/
│   ├── application.yml          # 主設定 (不含敏感資訊)
│   └── application-local.yml    # 本地 API Keys (gitignored)
├── Dockerfile
└── build.gradle
```

---

## License

MIT

---

## Data Attribution

- 氣象資料：[中央氣象署開放資料平臺](https://opendata.cwa.gov.tw)
- 空氣品質資料：[環境部環境資料開放平臺](https://data.moenv.gov.tw)
- YouBike 資料：[臺北市資料大平臺](https://data.taipei) + [新北市政府資料開放平臺](https://data.ntpc.gov.tw)
- 交通資料：[TDX 運輸資料流通服務平臺](https://tdx.transportdata.tw)

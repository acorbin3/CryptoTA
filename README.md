# CryptoTA
Cryptocurrency Technical Analysis for many exchanges and currencies

This project was used to learn Kotlin and more about creating Andorid apps when Crypto became popular ~2017. This origional was not going to be Open sources but I changed that later in the project. You can see the history of project here: https://github.com/acorbin3/CryptoTA/blob/master/app/src/main/java/com/backflippedstudios/crypto_ta/todoClass.kt

# 2026 Modernization
The app was modernized in August 2026:
- Toolchain: Gradle 8.13, Android Gradle Plugin 8.7, Kotlin 2.0, compileSdk/targetSdk 35, minSdk 21, full AndroidX migration
- Data source: [CoinGecko API](https://www.coingecko.com/en/api) replaces cryptowat.ch (shut down in 2023) and the old CoinMarketCap key. Coin catalog, OHLC candles, live prices, market-cap tab, and sparklines all come from CoinGecko's free tier (responses are cached and rate-limit aware)
- Firebase (Analytics, Messaging, Crashlytics) is optional: drop `app/google-services.json` from the Firebase console into the project and rebuild to enable it. Without the file the app builds and runs fine
- Removed: Fabric, Facebook SDK, Firestore, Room cache, Maoni (feedback now opens an email intent)

To build: open in Android Studio (or `./gradlew assembleDebug`) with JDK 17+.

Note on data granularity: CoinGecko's free OHLC endpoint decides candle size from the requested window (30 min candles for 1 day, 4 h for 2-30 days, 4 days for longer), so the time-period spinner maps to the nearest available granularity. Candle volume uses CoinGecko's rolling 24h volume series.

# Youtube videos of development
This project was also live streamed and the playlist can be found here: https://youtube.com/playlist?list=PLwnR6Orp5zUFHsgZUApGEqADjtzoXFfyo

# Playstore link
https://play.google.com/store/apps/details?id=com.backflippedstudios.crypto_ta&hl=en_US&gl=US

# Resources
This project leveraged [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for charting and [ta4j](https://github.com/ta4j/ta4j) with modifications to support other Tecnical analysis 
That source data comes from https://cryptowat.ch/

# Supported Technical Analysis algos

TA algos implemented:
- Volume Vars
- Average Directional Index
- Accumulation Distribution
- Awesome Oscillator
- Arron Up Down
- Arron Oscilator
- Chaikin Money Flow
- Candle Momentum Oscillator
- CCI
- Coppock Curve
- DOP
- Fisher Transform
- Mass Index
- Random Walking Index
- PPO
- Stoch Oscill
- Rate of Change
- RSI
- Williams R
- Ulcer Index
- Positive Volume
- Periodical Growth Rate
- MACD
- Negative Volume
- On Balance Volume
- RAVI
- Trailing Stop Loss
- Simple Linear Regression
- Standard Deviation
- Variance Indicator
- Piviot Point
- DeMark Piviot Point
- Fibonacci Reversal Support
- Fibonacci Reversal Resistance
- Bollinger Bands
- Bollinger Bands Width
- Bollinger Bands Percent B
- Candelier Exit Long
- Candelier Exit Short
- Exponential MA
- Exponential MA Ribbon
- Hull Moving Average
- Ichimoku Cloud
- Kafman Adaptive MA
- Keltner Channel
- Volume Weighted Average Price
- Moving Volume Weighted Average Price
- Parabolic SAR
- Simple Moving Average
- Zig Zag
- Zero Lag Moving Average

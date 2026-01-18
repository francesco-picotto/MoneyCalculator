# Money Calculator

A professional currency conversion application built with Java, implementing **Hexagonal Architecture** (Ports and Adapters) and **Domain-Driven Design (DDD)** principles. This project focuses on clean code, immutability, and financial precision.



## 🏗️ Architecture & Design

The project is structured into three distinct layers to ensure that business logic remains independent of external frameworks and infrastructure.

### 1. Domain Layer (The Core)
The heart of the application, containing business rules and logic.
* **Value Objects**: `Money`, `Currency`, and `ExchangeRate` are immutable and self-validating.
* **Precision**: Uses `BigDecimal` for all monetary calculations to avoid floating-point errors.
* **Rules**: Enforces domain constraints, such as prohibiting negative amounts or conversions between the same currency.

### 2. Application Layer (The Orchestration)
Coordinates the application's behavior through **Use Cases** and **Ports**.
* **Input Ports**: Interfaces like `ExchangeMoneyCommand` and `GetCurrenciesQuery` define the entry points.
* **Output Ports**: Interfaces like `ExchangeRateProvider` and `CurrencyRepository` define requirements for external data.
* **Use Cases**: `ExchangeMoneyUseCase` and `LoadCurrenciesUseCase` orchestrate the flow without knowing the technical details of the adapters.

### 3. Infrastructure Layer (The Adapters)
Contains the implementation of the output ports using specific technologies.
* **External API**: `ExchangeRateApiAdapter` connects to the **ExchangeRate-API** service.
* **Caching**: `CachedExchangeRateProvider` implements a **Decorator pattern** to provide time-based caching.
* **HTTP/JSON**: Custom implementations using Java 11 `HttpClient` and `Gson`.

## ✨ Features

* **Real-time Conversion**: Fetches live exchange rates for over 160 currencies via external REST APIs.
* **Precision Handling**: Exchange rates are normalized to 6 decimal places; money is normalized to 2.
* **Resilient Networking**: Built-in timeout management and comprehensive exception handling.
* **Performance Optimization**: Efficient caching strategy for both currency lists and exchange rates.
* **Responsive UI**: A Swing-based interface that uses background workers to keep the UI responsive during API calls.

## 🛠️ Technical Stack

* **Language**: Java 11+.
* **JSON Processing**: Google Gson.
* **HTTP Client**: Native Java 11 `HttpClient`.
* **GUI**: Java Swing.
* **Design Patterns**: Command, Adapter, Decorator, Value Object, and Dependency Injection.

## 🚀 Getting Started

### Prerequisites
* JDK 11 or higher.
* An API Key from [ExchangeRate-API](https://www.exchangerate-api.com).

### Configuration
Create an `application.properties` file in `src/main/resources`:
```properties
api.exchangerate.baseurl=[https://v6.exchangerate-api.com](https://v6.exchangerate-api.com)
api.exchangerate.key=YOUR_API_KEY
http.timeout.seconds=15
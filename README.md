# TaprobaneWheels - Alloy Wheel E-Commerce Ecosystem

TaprobaneWheels is a comprehensive solution for the alloy wheel retail industry, featuring a high-performance Android mobile application for consumers and a data-driven Web Admin Panel for business management.

## 📱 Mobile Application Features
a
The Android application provides a premium shopping experience tailored for wheel enthusiasts:

- **Advanced Search & Filtering:** Users can find the perfect wheels by filtering via Brand, Color (with color-coded previews), and Rim Size.
- **Dynamic Home Screen:** 
    - Auto-playing introductory video.
    - **Shake-to-Refresh:** Interactive sensor-based product updates.
    - Horizontal scrolling for "Top Selling" and "Featured" categories.
- **Seamless Checkout:** Integrated with **PayHere SDK** for secure and localized payment processing.
- **Store Locator:** Google Maps integration to find nearby physical showrooms.
- **Direct Support:** One-touch "Talk to Expert" dialing for technical assistance.
- **User Profile:** Personalized accounts with order history, wishlists, and cloud-synced profile images via Firebase.

## 📊 Web Admin Panel

The administrative backend is designed for efficient store management and business intelligence:

### Dashboard & Analytics
- **Key Performance Indicators (KPIs):** Real-time tracking of Daily/Monthly Revenue, Total Users, and Total Wheels Sold.
- **Inventory Insights:** Automatic alerts for low-stock items.
- **Sales Intelligence:** Identifies "Best Sellers," "Peak Revenue Month," and calculates "Average Order Value."
- **Revenue Visualization:** A 7-day bar chart overview emphasizing performance trends (e.g., identifying Tuesday as the highest-earning day).

### Management Modules
- **User Management:** Monitor and manage the customer base.
- **Brand Management:** Curation of wheel brands and manufacturers.
- **Product Management:** Full CRUD operations for the wheel catalog, including attribute management (Rim size, color, offsets).

## 🛠 Tech Stack

- **Platform:** Android (Java)
- **Backend:** Firebase (Firestore, Authentication, Storage)
- **Architecture:** Fragment-based UI with ViewBinding
- **Key Libraries:**
    - `Glide`: Image processing and caching.
    - `PayHere SDK`: Payment gateway integration.
    - `Google Maps API`: Location-based services.
    - `Lombok`: Reducing boilerplate code.
    - `Material Components`: Modern UI elements.

## 🚀 Getting Started

1.  **Android App:** Open the `EShop` project in Android Studio. Ensure you have the `google-services.json` file in the `app/` directory.
2.  **Firebase:** Configure a Firestore database with collections for `products`, `users`, `orders`, and `categories`.
3.  **Admin Panel:** Access the web dashboard via the provided URL (ensure the admin user has appropriate Firestore permissions).

---
*Developed for HDP2 - Alloy Wheel Management System.*

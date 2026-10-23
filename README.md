# Smart Pantry Management

# Smart Pantry Management

## Project Description

Smart Pantry Management is a Java Android application designed to help users reduce food waste by tracking ingredients available in their pantry and suggesting recipes they can prepare with those ingredients.

The application only suggests recipes when every required ingredient is available in the pantry in a sufficient quantity.

## Technologies Used

- Java
- Android Studio
- XML layouts
- SQLite
- Git and GitHub

## Database Choice

SQLite was selected because it is a local database supported by Android. It allows pantry items, recipes, recipe ingredients and quantities to be saved directly on the device. The data remains available after the application is closed and reopened.

## Main Features

- Add, view, edit and delete pantry ingredients
- Store quantities, units and optional expiry dates
- Browse suggested recipes based on strict ingredient matching
- View recipe ingredients and preparation steps
- Manage application preferences

## Database Design

The database contains three main tables:

- `pantry_items` — ingredients owned by the user
- `recipes` — recipe names and preparation methods
- `recipe_ingredients` — required ingredients, quantities and units for each recipe

## Setup and Running Instructions

1. Install Android Studio and a compatible Android SDK.
2. Clone or download this repository.
3. Open the project folder in Android Studio.
4. Allow Gradle to synchronise.
5. Start an Android emulator or connect a compatible Android device.
6. Run the application using the Android Studio Run button.

## GitHub Repository

https://github.com/Dinkwanyane/Smart-Pantry-Management.git

## Author

Dinkwanyane Phasha

402311055

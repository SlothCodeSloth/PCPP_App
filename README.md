# PC Part Picker Custom Android App

An Android App inspired by the website PCPartPicker that allows users to: 
- Create and manage **Lists** of computer components.
- Organize components into **Bundles** for easier tracking.
- View component details, pricing, vendor links, and images.
- Move components between lists and bundles.

---

## Features

- **Lists and Bundles**
  Organize components into lists and group them into bundles.

- **Detail Views**
  View component specifications, prices, and vendor URLs.

- **Add / Move Components**
  Add components to bundles, or move them between lists.

- **Edittng**
  Rename, reprice, or update vendor links for bundles.

- **Image Support*
  Component and Bundle images are displayed (and altered) using Glide.

---
## Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM
- **Database:** Room (with DAOs, entities, and relationships)
- **UI:** RecyclerView, Material Components
- **Asynchronous Tasks:** Coroutines + LifeCycleScope
- **Image Loading:** Glide

---

## Project Structure
```
com.example.pcpartpicker/
├── data/
│ ├── BundleEntity.kt 
│ ├── ComponentEntity.kt
│ ├── BundleComponentCrossRef.kt
│ ├── BundleWithComponents.kt
│ └── dao/
│ └── BundleDao.kt
│ └── ComponentDao.kt
├── ui/
│ ├── activities/
│ │ ├── BundleActivity.kt
│ │ └── DetailActivity.kt
│ ├── adapters/
│ │ ├── ComponentAdapter.kt
│ │ └── BundleComponentAdapter.kt
│ └── viewmodels/
│ └── PartViewModel.kt
└── MyApplication.kt
```
---
## Getting Started
1. Clone the repository.
2. Set up the Python Backend and API.
3. Set up the Android workspace.
4. Apply your Python API in the Android workspace (MyApplication.kt)
5. Run the Android App on an emulator or on a device.

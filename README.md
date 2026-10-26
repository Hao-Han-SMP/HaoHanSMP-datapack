# HaoHan-WorldGen

Plugin tạo thế giới Mặt Trăng (`haohan:lunar`) thuần Java dành cho server HaoHanSMP trên **Purpur / Paper 1.21.11** (Java 21).

---

## 🌟 Hệ thống WorldGen Thuần Java Hiện Đại

Toàn bộ thế giới Mặt Trăng đã được chuyển đổi hoàn toàn sang **Paper ChunkGenerator & BiomeProvider thuần Java** theo kiến trúc phân tầng độc lập dạng module:

### 1. Kiến trúc phân tầng (Modular Architecture)
* **`CellularNoise`**: Thuật toán Worley Noise 2D đa tầng, phân bổ hố thiên thạch và các bồn địa va chạm theo khoảng cách hình học chính xác.
* **`LunarTerrainPipeline`**:
  * **Biển Mặt Trăng (Maria)**: Đồng bằng dung nham bazan bằng phẳng (độ cao ~35).
  * **Cao nguyên (Highlands)**: Dãy núi anorthosite gồ ghề (độ cao ~68) với sóng terrain đa octave.
  * **Hệ thống hố thiên thạch đa quy mô**: Micro (r=3-7), Small (r=8-16), Large (r=20-36 có vành nâng), Mega Basins (r=50-80), và Complex Craters (có đỉnh phản hồi trung tâm - central rebound peak).
  * **Núi lửa & Caldera**: Nón núi lửa rộng với miệng sụp caldera ở đỉnh.
  * **Chống tạo gai (Anti-spike clamp)**: Khống chế vành hố giao nhau không vượt quá giới hạn an toàn.
* **`LunarSurfacePipeline` (Slope & Strata)**:
  * **Phát hiện độ dốc (Slope / Cliff Detection)**: Tính toán gradient địa hình tại chỗ. Nếu độ dốc vượt ngưỡng (`slant-threshold`), bụi xốp regolith trôi xuống và làm lộ vách đá dốc (`Cobbled Deepslate`, `Tuff`). Vùng phẳng tích tụ bụi bê tông mịn (`Concrete Powder`).
  * **Tầng địa chất (Strata)**: Lớp mặt phủ regolith (depth 0), lớp đất nén (depth 1-3) và đá nền bên dưới.
  * **Vết nứt Obsidian**: Các khe nứt obsidian và crying obsidian ở các tầng sâu đáy hố (Y <= 18).
* **`LunarBiomeProvider`**: Phân bổ mượt mà 7 biome Mặt Trăng (`lunar_maria`, `lunar_terrae`, `lunar_craters`, `lunar_bright_patches`, `lunar_crystal_craters`, `lunar_giant_crystals`, `lunar_giant_crystal_outskirts`).
* **`LunarPopulator`**: Tạo quặng (Anorthosite, Ilmenite, Kreep Basalt, Pyroxene), các cột tinh thể thạch anh tím (Amethyst Spire) và các tảng đá rơi văng (ejecta boulders).
* **`LunarStructurePopulator`**: Nạp và đặt trực tiếp các công trình `.nbt` từ resources (`rest_base`, `space_station`, `delta_abandoned_base`, các đảo và cột tinh thể) qua Paper `StructureManager`.

---

## ⚙️ Quản lý & Cấu hình trực tiếp (`worldgen.yml`)

Hệ thống tạo file `worldgen.yml` ngay trong thư mục plugin, cho phép điều chỉnh tham số mà không cần đụng vào file JSON phức tạp:
* Bật/tắt hố thiên thạch, núi lửa, quặng, tinh thể, tảng đá.
* Điều chỉnh tần suất hố (`frequency-scale`), độ sâu (`depth-scale`), độ nâng vành (`rim-scale`).
* Tùy biến ngưỡng dốc vách đá (`slant-threshold`) và vật liệu bề mặt.

### 🎮 Lệnh quản trị trong game:
* `/lunar info`: Hiển thị thông số cấu hình và trạng thái worldgen.
* `/lunar reload`: Tải lại cấu hình `worldgen.yml` ngay lập tức mà **không cần khởi động lại server**.
* `/lunar tp`: Dịch chuyển an toàn đến bề mặt Mặt Trăng.

---

## 📦 Cách cài đặt & Sử dụng

1. Build file `.jar` bằng Gradle hoặc chạy `./package.ps1` / `./package.sh`.
2. Copy file `out/HaoHan-WorldGen-1.0.0.jar` vào thư mục `plugins/` của server Purpur 1.21.11.
3. Khởi động server. Plugin sẽ tự động kích hoạt world generator và nạp thế giới `haohan:lunar`.

Plugin khai báo `provides: [HaoHanSMP-Datapack]`, do đó hoàn toàn tương thích và làm việc mượt mà với plugin gameplay `HaoHanLunar`.

---

## 🛠️ Build & Đóng gói

```bash
# Build Plugin Jar & Chạy Unit Tests
./gradlew build

# Đóng gói tự động
.\package.ps1   # Trên Windows PowerShell
./package.sh    # Trên Linux / macOS
```

---

## 📄 Giấy phép

Dự án được phát hành theo giấy phép [MIT](LICENSE).

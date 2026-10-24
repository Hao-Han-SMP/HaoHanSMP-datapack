# HaoHanSMP-datapack & Plugin

Hệ thống Datapack và Plugin nạp chiều không gian Mặt Trăng (`haohan:lunar`) dành cho server HaoHanSMP trên **Purpur / Paper 1.21.11** (Java 21).

---

## 🚀 Tính năng nổi bật (Plugin Mode)

Từ phiên bản này, hệ thống hỗ trợ chạy hoàn toàn dưới dạng **Purpur/Paper Plugin (`.jar`)**:
* **Không cần copy thủ công vào `world/datapacks/`**: Plugin tự động đăng ký bundled datapack thông qua Paper Lifecycle Event (`DATAPACK_DISCOVERY`).
* **Tự động load Dimension**: Tự động gọi `WorldCreator` nạp chiều không gian `haohan:lunar` ngay khi server khởi động.
* **Lệnh kiểm tra trực tiếp**: `/haohandatapack` (hoặc `/hhd`) để xem trạng thái nạp thế giới Mặt Trăng và datapack.

---

## 📦 Cách cài đặt & Sử dụng

### Cách 1: Sử dụng dưới dạng Plugin (Khuyên dùng)
1. Build file `.jar` bằng Gradle hoặc chạy `./package.ps1` / `./package.sh`.
2. Copy file `HaoHanSMP-datapack-1.0.0.jar` vào thư mục `plugins/` của server Purpur 1.21.11.
3. Khởi động server. Plugin sẽ tự động kích hoạt datapack và nạp thế giới `haohan:lunar`.

### Cách 2: Sử dụng dưới dạng Datapack truyền thống (World folder)
1. Chạy script đóng gói để tạo file `.zip` trong thư mục `out/`.
2. Bỏ file `.zip` vào thư mục `<server_root>/world/datapacks/`.
3. Khởi động server hoặc chạy `/reload`.

---

## 🛠️ Build & Đóng gói

Dự án cung cấp sẵn Gradle Wrapper và script đóng gói:

### Build Plugin Jar với Gradle:
```bash
./gradlew build
# File output: build/libs/HaoHanSMP-datapack-1.0.0.jar
```

### Đóng gói tự động (cả Jar và Zip):
* **Windows (PowerShell):**
  ```powershell
  .\package.ps1
  ```
* **Linux / macOS:**
  ```bash
  chmod +x package.sh
  ./package.sh
  ```
  *(Các file thành phẩm sẽ xuất hiện trong thư mục `out/`)*

---

## 📄 Giấy phép

Dự án được phát hành theo giấy phép [MIT](LICENSE).

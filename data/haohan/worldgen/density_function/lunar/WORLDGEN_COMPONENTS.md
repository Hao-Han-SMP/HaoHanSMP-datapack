# Lunar worldgen components

`data/haohan/worldgen/noise_settings/lunar.json` chỉ giữ cấu hình chung của dimension:
độ cao, độ phân giải lấy mẫu, noise router và quy tắc vật liệu bề mặt.

Density được ghép theo thứ tự:

1. `lunar/final_density.json` — gradient theo trục Y và entrypoint tổng.
2. `lunar/components.json` — nơi ghép các hệ worldgen độc lập.
3. `lunar/terrain/` — nền địa hình tự nhiên, chia theo biome.
4. `lunar/meteor_hole/` — toàn bộ hình dạng hố thiên thạch.
5. `lunar/volcano/` — núi lửa cao, sườn rộng và có caldera.
6. `lunar/biome/` — density function cho biome selection (multi_noise).

`components.json` cộng phần đáy crater riêng, sau đó dùng `max` giữa vành crater
và núi lửa. Cách ghép này ngăn hai phần nhô cao cộng chồng thành trụ đá.

## Biome Selection

Dùng `minecraft:multi_noise` biome source với 3 biome:

- **lunar_maria** — Biển Mặt Trăng: vùng tối, bằng phẳng, dung nham bazan cổ.
  Chọn khi `continents` thấp (-1.0 → -0.1).
- **lunar_terrae** — Vùng đất cao: cao nguyên sáng, gồ ghề, đá anorthosit.
  Chọn khi `continents` cao (0.15 → 1.0) và `erosion` thấp (-1.0 → 0.35).
- **lunar_craters** — Hố va chạm: bồn địa va chạm lớn kiểu Nam Cực-Aitken.
  Chọn khi `erosion` cao (0.45 → 1.0), bất kể continents.

Noise cho biome selection nằm trong `data/haohan/worldgen/noise/lunar/biome/`:
- `continents.json` — `firstOctave: -9`, tạo vùng rộng 700–1500 blocks.
- `erosion.json` — `firstOctave: -10`, vùng crater cực rộng.

Density function tương ứng nằm trong `lunar/biome/continents.json` và
`lunar/biome/erosion.json`, được trỏ từ noise_router.

## Meteor holes

- `micro.json`: các hố nhỏ, nông và xuất hiện dày nhất.
- `small.json`: hố nhỏ.
- `large.json`: hố lớn.
- `mega.json`: hố siêu lớn, dùng noise `firstOctave: -9`.
- `basin.json`: impact basin siêu siêu lớn như ảnh tham khảo, giữ profile cũ và
  dùng noise `firstOctave: -11`; có thể chỉnh độc lập với mega.
- `complex.json`: hố tròn có vành và đỉnh trung tâm.
- `major_depressions.json`: chọn đáy sâu nhất giữa large, mega, basin và complex.
- `minor_depressions.json`: chọn đáy sâu nhất giữa micro và small.
- `depressions.json`: ghép hai nhóm để hố nhỏ vẫn xuất hiện trong đại hố.
- `reliefs.json`: chọn vành cao nhất khi nhiều hố giao nhau.
- `offset.json`: entrypoint của toàn bộ hệ hố thiên thạch.
- `cache/`: cache từng profile 2D để không tính lại ở nhánh đáy và nhánh vành.

Noise tương ứng nằm trong `data/haohan/worldgen/noise/lunar/meteor_hole/`.

## Terrain

Terrain chia theo biome, blend bằng spline trên continents noise:

- `base_relief.json` — spline dùng `lunar/biome/continents` làm coordinate,
  lerp giữa `mare_relief` (Maria) và `highlands_relief` (Terrae).
- `mare_relief.json` — đồng bằng bazan phẳng, spline hẹp (-0.14 → 0.0),
  chỉ có mid noise nhẹ, không có detail/highlands noise.
- `highlands_relief.json` — cao nguyên gồ ghề, spline cao (0.12 → 0.48),
  dùng rolling + mid + detail + highlands noise cho texture phong phú.

Noise terrain nằm trong `data/haohan/worldgen/noise/lunar/terrain/`:
- `rolling.json` — sóng rộng cho nền terrain (`firstOctave: -9`).
- `mid.json` — sóng trung bình (`firstOctave: -6`).
- `detail.json` — chi tiết nhỏ (`firstOctave: -3`).
- `highlands.json` — gồ ghề riêng cho Terrae (`firstOctave: -5`).

## Surface Rules

Mỗi biome có vật liệu bề mặt riêng biệt dựa trên `biome` condition:

- **Maria**: basalt (chính), smooth_basalt, deepslate — mô phỏng dung nham bazan.
- **Terrae**: calcite (chính), tuff, andesite — mô phỏng đá anorthosit sáng.
- **Craters**: tuff (chính), smooth_basalt, deepslate, andesite — hỗn hợp regolith
  bị nghiền nát do va chạm.

Surface mix dùng noise `haohan:lunar/surface_mix` để tạo texture đa dạng cho
mỗi biome.

## Volcano

- `large.json`: profile núi lửa lớn, nón rộng và tâm caldera thấp hơn; không có
  needle peak hẹp để tránh pillar một ô.
- `offset.json`: entrypoint của hệ núi lửa.

`preliminary_surface_level` dùng `minecraft:zero`; surface rule không phụ thuộc
`above_preliminary_surface`, nên tránh được một lượt tính toàn bộ density dư thừa.

Noise tương ứng nằm trong `data/haohan/worldgen/noise/lunar/volcano/`.

Các thành phần tương lai như `cave/` hoặc `dungeon/` nên có entrypoint
riêng, sau đó được nối tại `lunar/components.json`.

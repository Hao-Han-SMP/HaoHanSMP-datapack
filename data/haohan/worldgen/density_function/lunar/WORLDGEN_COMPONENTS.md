# Lunar worldgen components

`data/haohan/worldgen/noise_settings/lunar.json` chỉ giữ cấu hình chung của dimension:
độ cao, độ phân giải lấy mẫu, noise router và quy tắc vật liệu bề mặt.

Density được ghép theo thứ tự:

1. `lunar/final_density.json` — gradient theo trục Y và entrypoint tổng.
2. `lunar/components.json` — nơi ghép các hệ worldgen độc lập.
3. `lunar/terrain/` — nền địa hình tự nhiên.
4. `lunar/meteor_hole/` — toàn bộ hình dạng hố thiên thạch.
5. `lunar/volcano/` — núi lửa cao, sườn rộng và có caldera.

`components.json` cộng phần đáy crater riêng, sau đó dùng `max` giữa vành crater
và núi lửa. Cách ghép này ngăn hai phần nhô cao cộng chồng thành trụ đá.

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

`terrain/base_relief.json` dùng rolling noise bất đối xứng: phía thấp tạo các đồng
bằng mare rộng, phía cao nâng thành lunar highlands. Mid/detail chỉ thêm gợn nhẹ,
không tạo thêm một trường noise mới.

## Volcano

- `large.json`: profile núi lửa lớn, nón rộng và tâm caldera thấp hơn; không có
  needle peak hẹp để tránh pillar một ô.
- `offset.json`: entrypoint của hệ núi lửa.

`preliminary_surface_level` dùng `minecraft:zero`; surface rule không phụ thuộc
`above_preliminary_surface`, nên tránh được một lượt tính toàn bộ density dư thừa.

Noise tương ứng nằm trong `data/haohan/worldgen/noise/lunar/volcano/`.

Các thành phần tương lai như `cave/` hoặc `dungeon/` nên có entrypoint
riêng, sau đó được nối tại `lunar/components.json`.

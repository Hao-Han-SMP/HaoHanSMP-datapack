import { gzipSync } from "node:zlib";
import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const SIZE = [48, 48, 48];
const SURFACE_SIZE = [40, 52, 40];
const OUTSKIRT_SIZE = [24, 32, 24];
const DATA_VERSION = 4671;
const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const OUTPUT = resolve(ROOT, "data/haohan/structure/crystal_crater");
const SURFACE_OUTPUT = resolve(ROOT, "data/haohan/structure/giant_crystal_field");
const OUTSKIRT_OUTPUT = resolve(ROOT, "data/haohan/structure/giant_crystal_outskirts");

const palettes = [
  "minecraft:air",
  "minecraft:calcite",
  "minecraft:smooth_quartz",
  "minecraft:quartz_block",
  "minecraft:amethyst_block",
  "minecraft:budding_amethyst",
  "minecraft:purple_stained_glass",
  "minecraft:tinted_glass",
  "minecraft:crying_obsidian",
  "minecraft:sea_lantern",
  "minecraft:white_stained_glass",
  "minecraft:light_blue_stained_glass",
  "minecraft:blue_stained_glass",
  "minecraft:cyan_stained_glass",
  "minecraft:magenta_stained_glass",
  "minecraft:pink_stained_glass",
  "minecraft:lime_stained_glass",
  "minecraft:orange_stained_glass",
  "minecraft:pearlescent_froglight",
  "minecraft:verdant_froglight",
  "minecraft:ochre_froglight",
  {
    key: "amethyst_cluster_up",
    name: "minecraft:amethyst_cluster",
    properties: {
      facing: "up",
      waterlogged: "false"
    }
  },
  {
    key: "amethyst_cluster_down",
    name: "minecraft:amethyst_cluster",
    properties: {
      facing: "down",
      waterlogged: "false"
    }
  },
  {
    key: "amethyst_cluster_north",
    name: "minecraft:amethyst_cluster",
    properties: {
      facing: "north",
      waterlogged: "false"
    }
  },
  {
    key: "amethyst_cluster_south",
    name: "minecraft:amethyst_cluster",
    properties: {
      facing: "south",
      waterlogged: "false"
    }
  },
  {
    key: "amethyst_cluster_east",
    name: "minecraft:amethyst_cluster",
    properties: {
      facing: "east",
      waterlogged: "false"
    }
  },
  {
    key: "amethyst_cluster_west",
    name: "minecraft:amethyst_cluster",
    properties: {
      facing: "west",
      waterlogged: "false"
    }
  }
];

const paletteKey = (entry) => typeof entry === "string" ? entry : entry.key;
const blockIndex = new Map(palettes.map((entry, index) => [paletteKey(entry), index]));

const colorThemes = [
  [
    "minecraft:light_blue_stained_glass",
    "minecraft:cyan_stained_glass",
    "minecraft:lime_stained_glass",
    "minecraft:orange_stained_glass",
    "minecraft:pink_stained_glass",
    "minecraft:purple_stained_glass"
  ],
  [
    "minecraft:white_stained_glass",
    "minecraft:light_blue_stained_glass",
    "minecraft:cyan_stained_glass",
    "minecraft:purple_stained_glass",
    "minecraft:pink_stained_glass"
  ],
  [
    "minecraft:purple_stained_glass",
    "minecraft:magenta_stained_glass",
    "minecraft:pink_stained_glass",
    "minecraft:white_stained_glass",
    "minecraft:tinted_glass"
  ]
];

function u16(value) {
  const out = Buffer.alloc(2);
  out.writeUInt16BE(value);
  return out;
}

function i32(value) {
  const out = Buffer.alloc(4);
  out.writeInt32BE(value);
  return out;
}

function stringPayload(value) {
  const text = Buffer.from(value, "utf8");
  return Buffer.concat([u16(text.length), text]);
}

function named(type, name, payload) {
  return Buffer.concat([Buffer.from([type]), stringPayload(name), payload]);
}

function intTag(name, value) {
  return named(3, name, i32(value));
}

function stringTag(name, value) {
  return named(8, name, stringPayload(value));
}

function listTag(name, elementType, entries) {
  return named(9, name, Buffer.concat([
    Buffer.from([elementType]),
    i32(entries.length),
    ...entries
  ]));
}

function compoundPayload(tags) {
  return Buffer.concat([...tags, Buffer.from([0])]);
}

function compoundTag(name, tags) {
  return named(10, name, compoundPayload(tags));
}

function key(x, y, z) {
  return `${x},${y},${z}`;
}

function hash3(x, y, z, seed) {
  let n = Math.imul(x + seed * 17, 374761393)
    ^ Math.imul(y - seed * 29, 668265263)
    ^ Math.imul(z + seed * 43, 2147483647);
  n = Math.imul(n ^ (n >>> 13), 1274126177);
  return (n ^ (n >>> 16)) >>> 0;
}

function randomSource(seed) {
  let value = (seed + 1) * 0x6d2b79f5;
  return () => {
    value += 0x6d2b79f5;
    let n = value;
    n = Math.imul(n ^ (n >>> 15), n | 1);
    n ^= n + Math.imul(n ^ (n >>> 7), n | 61);
    return ((n ^ (n >>> 14)) >>> 0) / 4294967296;
  };
}

function caveValue(x, y, z, seed) {
  const wobbleX = Math.sin((z + seed * 7) * 0.23) * 1.4;
  const wobbleZ = Math.cos((x - seed * 5) * 0.21) * 1.2;
  const dx = (x - 24 + wobbleX) / 20;
  const dy = (y - 16) / 11;
  const dz = (z - 24 + wobbleZ) / 18;
  return dx * dx + dy * dy + dz * dz;
}

function normalize(vector) {
  const length = Math.hypot(...vector);
  return vector.map((value) => value / length);
}

function crystalFrame(a, b, rotation) {
  const axis = normalize([b[0] - a[0], b[1] - a[1], b[2] - a[2]]);
  const helper = Math.abs(axis[1]) < 0.86 ? [0, 1, 0] : [1, 0, 0];
  const side = normalize([
    axis[1] * helper[2] - axis[2] * helper[1],
    axis[2] * helper[0] - axis[0] * helper[2],
    axis[0] * helper[1] - axis[1] * helper[0]
  ]);
  const up = [
    axis[1] * side[2] - axis[2] * side[1],
    axis[2] * side[0] - axis[0] * side[2],
    axis[0] * side[1] - axis[1] * side[0]
  ];
  const cosine = Math.cos(rotation);
  const sine = Math.sin(rotation);
  return {
    axis,
    side: side.map((value, index) => value * cosine + up[index] * sine),
    up: up.map((value, index) => value * cosine - side[index] * sine),
    length: Math.hypot(b[0] - a[0], b[1] - a[1], b[2] - a[2])
  };
}

function crystalCoordinates(x, y, z, a, frame) {
  const offset = [x - a[0], y - a[1], z - a[2]];
  const dot = (left, right) =>
    left[0] * right[0] + left[1] * right[1] + left[2] * right[2];
  return {
    along: dot(offset, frame.axis),
    side: dot(offset, frame.side),
    up: dot(offset, frame.up)
  };
}

function polygonDistance(side, up, facets) {
  let distance = -Infinity;
  for (let face = 0; face < facets; face++) {
    const angle = face * Math.PI * 2 / facets;
    distance = Math.max(distance, side * Math.cos(angle) + up * Math.sin(angle));
  }
  return distance;
}

function variantSpikes(seed) {
  const variants = [
    [
      [[5, 12, 20], [40, 22, 31], 2.35, 5],
      [[40, 8, 33], [8, 24, 15], 2.15, 4],
      [[17, 4, 14], [31, 27, 31], 1.95, 5],
      [[35, 27, 17], [13, 6, 33], 1.8, 6],
      [[6, 20, 31], [36, 10, 17], 1.65, 5],
      [[39, 21, 16], [15, 11, 35], 1.55, 4]
    ],
    [
      [[7, 7, 30], [39, 22, 14], 2.3, 4],
      [[40, 17, 13], [8, 20, 34], 2.15, 5],
      [[16, 3, 18], [31, 27, 29], 2.0, 6],
      [[36, 27, 28], [13, 7, 17], 1.85, 5],
      [[7, 18, 15], [38, 11, 33], 1.7, 4],
      [[35, 7, 38], [15, 23, 12], 1.55, 5]
    ],
    [
      [[6, 15, 34], [40, 18, 17], 2.4, 6],
      [[41, 10, 20], [8, 23, 29], 2.15, 5],
      [[27, 3, 12], [18, 27, 33], 1.95, 4],
      [[15, 27, 31], [34, 6, 17], 1.85, 5],
      [[8, 8, 17], [38, 23, 34], 1.7, 6],
      [[39, 22, 35], [12, 9, 14], 1.55, 4]
    ]
  ];
  return variants[seed];
}

function caveFloorAndCeiling(x, z, seed) {
  const wobbleX = Math.sin((z + seed * 7) * 0.23) * 1.4;
  const wobbleZ = Math.cos((x - seed * 5) * 0.21) * 1.2;
  const dx = (x - 24 + wobbleX) / 20;
  const dz = (z - 24 + wobbleZ) / 18;
  const horizontal = dx * dx + dz * dz;
  if (horizontal >= 0.92) return null;
  const halfHeight = 11 * Math.sqrt(1 - horizontal);
  return {
    floor: 16 - halfHeight,
    ceiling: 16 + halfHeight
  };
}

function proceduralSpikes(seed) {
  const random = randomSource(701 + seed * 131);
  const colors = colorThemes[seed];
  const spikes = [];
  const addFloorOrCeiling = (fromCeiling) => {
    for (let attempts = 0; attempts < 120; attempts++) {
      if (spikes.filter((spike) => spike.kind === (fromCeiling ? "ceiling" : "floor")).length
        >= (fromCeiling ? 6 : 8)) break;
      const x = 7 + random() * 34;
      const z = 7 + random() * 34;
      const bounds = caveFloorAndCeiling(x, z, seed);
      if (!bounds || bounds.ceiling - bounds.floor < 12) continue;
      const available = bounds.ceiling - bounds.floor - 2;
      const length = Math.min(available * 0.8, 8 + random() * 9);
      const baseY = fromCeiling ? bounds.ceiling - 1.2 : bounds.floor + 1.2;
      const tipY = baseY + (fromCeiling ? -length : length);
      const lean = 3.5 + random() * 5.5;
      const angle = random() * Math.PI * 2;
      spikes.push({
        a: [x, baseY, z],
        b: [
          x + Math.cos(angle) * lean,
          tipY,
          z + Math.sin(angle) * lean
        ],
        radius: 0.78 + random() * 0.72,
        facets: 4 + Math.floor(random() * 3),
        shell: colors[Math.floor(random() * colors.length)],
        kind: fromCeiling ? "ceiling" : "floor"
      });
    }
  };

  addFloorOrCeiling(false);
  addFloorOrCeiling(true);

  for (let i = 0; i < 6; i++) {
    const angle = random() * Math.PI * 2;
    const y = 7 + random() * 17;
    const yFactor = Math.sqrt(Math.max(0.15, 1 - ((y - 16) / 11) ** 2));
    const base = [
      24 + Math.cos(angle) * (19 * yFactor - 1.2),
      y,
      24 + Math.sin(angle) * (17 * yFactor - 1.2)
    ];
    const inward = 10 + random() * 9;
    spikes.push({
      a: base,
      b: [
        base[0] - Math.cos(angle) * inward,
        y + (random() - 0.5) * 10,
        base[2] - Math.sin(angle) * inward
      ],
      radius: 0.75 + random() * 0.7,
      facets: 4 + Math.floor(random() * 3),
      shell: colors[Math.floor(random() * colors.length)],
      kind: "wall"
    });
  }

  return spikes;
}

function generate(seed) {
  const blocks = new Map();
  const set = (x, y, z, name) => {
    if (x >= 0 && x < 48 && y >= 0 && y < 48 && z >= 0 && z < 48) {
      blocks.set(key(x, y, z), { pos: [x, y, z], state: blockIndex.get(name) });
    }
  };

  for (let x = 0; x < 48; x++) {
    for (let z = 0; z < 48; z++) {
      const dx = x - 23.5;
      const dz = z - 23.5;
      const radius = Math.hypot(dx, dz);
      const angularNoise = Math.sin(Math.atan2(dz, dx) * (5 + seed) + seed) * 0.7;
      const rimRadius = radius + angularNoise;

      if (rimRadius <= 22.5) {
        const depth = 12 * Math.pow(Math.max(0, 1 - (rimRadius / 22.5) ** 2), 1.35);
        const floorY = Math.floor(40 - depth);
        for (let y = floorY + 1; y < 48; y++) set(x, y, z, "minecraft:air");
        const floorNoise = hash3(x, floorY, z, seed) % 13;
        const floorBlock = floorNoise === 0
          ? "minecraft:amethyst_block"
          : floorNoise < 5
            ? "minecraft:smooth_quartz"
            : "minecraft:calcite";
        set(x, floorY, z, floorBlock);
      }

      if (rimRadius > 20.5 && rimRadius < 24.0) {
        const rimY = 40 + Math.max(0, Math.floor(3.2 - Math.abs(rimRadius - 22.2) * 2));
        const rimNoise = hash3(x, rimY, z, seed + 11) % 9;
        const rimBlock = rimNoise === 0
          ? "minecraft:purple_stained_glass"
          : rimNoise < 3
            ? "minecraft:amethyst_block"
            : rimNoise < 6
              ? "minecraft:smooth_quartz"
              : "minecraft:calcite";
        set(x, rimY, z, rimBlock);
      }
    }
  }

  for (let x = 0; x < 48; x++) {
    for (let y = 1; y < 31; y++) {
      for (let z = 0; z < 48; z++) {
        const value = caveValue(x, y, z, seed);
        if (value <= 1.0) {
          set(x, y, z, "minecraft:air");
        } else if (value <= 1.13) {
          const n = hash3(x, y, z, seed + 23) % 20;
          const shell = n === 0
            ? "minecraft:budding_amethyst"
            : n < 4
              ? "minecraft:amethyst_block"
              : n < 10
                ? "minecraft:smooth_quartz"
                : "minecraft:calcite";
          set(x, y, z, shell);
        }
      }
    }
  }

  for (let y = 17; y <= 47; y++) {
    const t = (y - 17) / 30;
    const cx = 20 + t * 4.5 + Math.sin((y + seed * 4) * 0.42) * (1.7 + t);
    const cz = 27 - t * 3.0 + Math.cos((y - seed * 3) * 0.38) * (1.5 + t * 0.8);
    const radius = 3.4 + t * 1.8;
    for (let x = Math.floor(cx - radius); x <= Math.ceil(cx + radius); x++) {
      for (let z = Math.floor(cz - radius); z <= Math.ceil(cz + radius); z++) {
        if (Math.hypot(x - cx, z - cz) <= radius) set(x, y, z, "minecraft:air");
      }
    }
  }

  const renderSpike = (a, b, baseRadius, shellBlock, addCluster, facets, crystalId) => {
    const frame = crystalFrame(a, b, (crystalId * 1.618 + seed * 0.73) % (Math.PI * 2));
    const padding = Math.ceil(baseRadius + 1);
    const minX = Math.max(1, Math.floor(Math.min(a[0], b[0]) - padding));
    const maxX = Math.min(46, Math.ceil(Math.max(a[0], b[0]) + padding));
    const minY = Math.max(1, Math.floor(Math.min(a[1], b[1]) - padding));
    const maxY = Math.min(30, Math.ceil(Math.max(a[1], b[1]) + padding));
    const minZ = Math.max(1, Math.floor(Math.min(a[2], b[2]) - padding));
    const maxZ = Math.min(46, Math.ceil(Math.max(a[2], b[2]) + padding));
    for (let x = minX; x <= maxX; x++) {
      for (let y = minY; y <= maxY; y++) {
        for (let z = minZ; z <= maxZ; z++) {
          const coordinates = crystalCoordinates(x + 0.5, y + 0.5, z + 0.5, a, frame);
          const t = coordinates.along / frame.length;
          if (t < -0.035 || t > 1.035) continue;
          const shoulder = 0.73;
          const profile = t <= shoulder
            ? 1.0 - 0.08 * Math.max(0, t) / shoulder
            : Math.max(0.025, 0.92 * (1.0 - t) / (1.0 - shoulder));
          const radius = Math.max(0.12, baseRadius * profile);
          const distance = polygonDistance(coordinates.side, coordinates.up, facets);
          if (distance <= radius + 0.2 && caveValue(x, y, z, seed) < 0.99) {
            if (distance > Math.max(0, radius - 0.52)) {
              const shell = hash3(x, y, z, seed + 71) % 6 === 0
                ? "minecraft:tinted_glass"
                : shellBlock;
              set(x, y, z, shell);
            } else {
              const band = Math.floor(t * 11 + seed) % 7;
              const lightBlock = seed === 0
                ? "minecraft:verdant_froglight"
                : seed === 1
                  ? "minecraft:sea_lantern"
                  : "minecraft:pearlescent_froglight";
              const core = band === 1
                ? "minecraft:amethyst_block"
                : band === 3 && baseRadius > 1.25
                  ? lightBlock
                  : band === 5
                  ? "minecraft:budding_amethyst"
                  : "minecraft:quartz_block";
              set(x, y, z, core);
            }
          }
        }
      }
    }
    if (addCluster) {
      const direction = [b[0] - a[0], b[1] - a[1], b[2] - a[2]];
      const axis = direction
        .map((value, index) => ({ value: Math.abs(value), index }))
        .sort((left, right) => right.value - left.value)[0].index;
      let cluster = direction[axis] >= 0
        ? ["amethyst_cluster_east", "amethyst_cluster_up", "amethyst_cluster_south"][axis]
        : ["amethyst_cluster_west", "amethyst_cluster_down", "amethyst_cluster_north"][axis];
      set(Math.round(b[0]), Math.round(b[1]), Math.round(b[2]), cluster);
    }
  };

  variantSpikes(seed).forEach(([a, b, baseRadius, facets], index) => {
    const colors = colorThemes[seed];
    renderSpike(a, b, baseRadius, colors[index % colors.length], false, facets, index);
  });

  const smallSpikes = proceduralSpikes(seed);
  smallSpikes.forEach(({ a, b, radius, facets, shell }, index) => {
    renderSpike(a, b, radius, shell, index % 9 === 0, facets, index + 17);
  });

  for (const [x, y, z] of [[24, 8, 24], [13, 12, 25], [35, 14, 27]]) {
    if (blocks.get(key(x, y, z))?.state === blockIndex.get("minecraft:air")) {
      set(x, y, z, "minecraft:sea_lantern");
    }
  }

  return [...blocks.values()];
}

function surfaceSpireVariants(seed) {
  const variants = [
    [
      [[20, 0, 20], [27, 47, 14], 4.45, 5, "minecraft:light_blue_stained_glass"],
      [[12, 0, 25], [6, 33, 33], 2.9, 4, "minecraft:cyan_stained_glass"],
      [[29, 0, 25], [36, 29, 32], 2.65, 6, "minecraft:purple_stained_glass"],
      [[27, 0, 11], [34, 23, 6], 2.15, 5, "minecraft:white_stained_glass"],
      [[11, 0, 12], [5, 20, 7], 1.9, 4, "minecraft:cyan_stained_glass"],
      [[18, 0, 31], [15, 16, 37], 1.65, 5, "minecraft:purple_stained_glass"]
    ],
    [
      [[20, 0, 20], [13, 48, 27], 4.6, 6, "minecraft:white_stained_glass"],
      [[29, 0, 15], [36, 34, 9], 3.0, 5, "minecraft:light_blue_stained_glass"],
      [[11, 0, 14], [5, 28, 7], 2.6, 4, "minecraft:pink_stained_glass"],
      [[25, 0, 30], [31, 24, 36], 2.2, 5, "minecraft:cyan_stained_glass"],
      [[12, 0, 29], [7, 20, 35], 1.9, 6, "minecraft:white_stained_glass"],
      [[30, 0, 25], [36, 16, 28], 1.7, 4, "minecraft:light_blue_stained_glass"]
    ],
    [
      [[20, 0, 20], [28, 46, 28], 4.5, 5, "minecraft:purple_stained_glass"],
      [[12, 0, 25], [5, 35, 18], 3.05, 6, "minecraft:magenta_stained_glass"],
      [[28, 0, 12], [36, 29, 6], 2.65, 4, "minecraft:pink_stained_glass"],
      [[11, 0, 12], [5, 24, 6], 2.2, 5, "minecraft:tinted_glass"],
      [[27, 0, 30], [34, 20, 36], 1.9, 6, "minecraft:purple_stained_glass"],
      [[17, 0, 31], [14, 16, 37], 1.7, 4, "minecraft:magenta_stained_glass"]
    ]
  ];
  return variants[seed];
}

function outskirtSpireVariants(seed) {
  const variants = [
    [
      [[12, 0, 12], [16, 27, 8], 2.75, 5, "minecraft:light_blue_stained_glass"],
      [[6, 0, 15], [3, 17, 20], 1.65, 4, "minecraft:cyan_stained_glass"],
      [[17, 0, 17], [21, 13, 21], 1.3, 6, "minecraft:purple_stained_glass"]
    ],
    [
      [[12, 0, 12], [8, 26, 17], 2.8, 6, "minecraft:white_stained_glass"],
      [[18, 0, 8], [21, 18, 4], 1.7, 5, "minecraft:light_blue_stained_glass"],
      [[7, 0, 17], [4, 12, 21], 1.3, 4, "minecraft:pink_stained_glass"]
    ],
    [
      [[12, 0, 12], [17, 28, 17], 2.8, 5, "minecraft:purple_stained_glass"],
      [[7, 0, 16], [3, 18, 12], 1.75, 6, "minecraft:magenta_stained_glass"],
      [[17, 0, 7], [21, 13, 4], 1.35, 4, "minecraft:pink_stained_glass"]
    ]
  ];
  return variants[seed];
}

function generateSurfaceSpire(seed, size, spires, saltOffset = 0) {
  const blocks = new Map();
  const [width, height, depth] = size;
  const set = (x, y, z, name) => {
    if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
      blocks.set(key(x, y, z), { pos: [x, y, z], state: blockIndex.get(name) });
    }
  };

  spires.forEach(([a, b, baseRadius, facets, shellBlock], crystalId) => {
    const frame = crystalFrame(a, b, (crystalId * 1.731 + seed * 0.91) % (Math.PI * 2));
    const padding = Math.ceil(baseRadius + 1);
    const minX = Math.max(0, Math.floor(Math.min(a[0], b[0]) - padding));
    const maxX = Math.min(width - 1, Math.ceil(Math.max(a[0], b[0]) + padding));
    const minY = 0;
    const maxY = Math.min(height - 1, Math.ceil(Math.max(a[1], b[1]) + padding));
    const minZ = Math.max(0, Math.floor(Math.min(a[2], b[2]) - padding));
    const maxZ = Math.min(depth - 1, Math.ceil(Math.max(a[2], b[2]) + padding));

    for (let x = minX; x <= maxX; x++) {
      for (let y = minY; y <= maxY; y++) {
        for (let z = minZ; z <= maxZ; z++) {
          const coordinates = crystalCoordinates(x + 0.5, y + 0.5, z + 0.5, a, frame);
          const t = coordinates.along / frame.length;
          if (t < -0.06 || t > 1.035) continue;
          const shoulder = 0.7;
          const profile = t <= shoulder
            ? 1.0 - 0.1 * Math.max(0, t) / shoulder
            : Math.max(0.02, 0.9 * (1.0 - t) / (1.0 - shoulder));
          const radius = Math.max(0.12, baseRadius * profile);
          const distance = polygonDistance(coordinates.side, coordinates.up, facets);
          if (distance > radius + 0.2) continue;

          if (distance > Math.max(0, radius - 0.62)) {
            const shell = hash3(x, y, z, 211 + saltOffset + seed * 37 + crystalId) % 7 === 0
              ? "minecraft:tinted_glass"
              : shellBlock;
            set(x, y, z, shell);
          } else {
            const band = Math.floor(t * 13 + seed + crystalId) % 8;
            const lightBlock = seed === 0
              ? "minecraft:verdant_froglight"
              : seed === 1
                ? "minecraft:sea_lantern"
                : "minecraft:pearlescent_froglight";
            const core = band === 1
              ? "minecraft:amethyst_block"
              : band === 3 && baseRadius > 2.0
                ? lightBlock
                : band === 6
                  ? "minecraft:budding_amethyst"
                  : "minecraft:quartz_block";
            set(x, y, z, core);
          }
        }
      }
    }

    const rootRadius = Math.ceil(baseRadius + 1.2);
    for (let x = Math.floor(a[0] - rootRadius); x <= Math.ceil(a[0] + rootRadius); x++) {
      for (let y = 0; y <= Math.min(3, Math.ceil(baseRadius)); y++) {
        for (let z = Math.floor(a[2] - rootRadius); z <= Math.ceil(a[2] + rootRadius); z++) {
          const horizontal = Math.hypot(x + 0.5 - a[0], z + 0.5 - a[2]);
          const limit = rootRadius * (1.0 - y / 5.5);
          if (horizontal <= limit
            && hash3(x, y, z, saltOffset + seed + crystalId * 19) % 7 !== 0) {
            const rootBlock = hash3(x, y, z, saltOffset + seed + 503) % 5 === 0
              ? "minecraft:amethyst_block"
              : "minecraft:calcite";
            set(x, y, z, rootBlock);
          }
        }
      }
    }
  });

  return [...blocks.values()];
}

function structureNbt(blocks, size = SIZE) {
  const sizeList = size.map(i32);
  const paletteList = palettes.map((entry) => {
    if (typeof entry === "string") {
      return compoundPayload([stringTag("Name", entry)]);
    }
    const propertyTags = Object.entries(entry.properties).map(([name, value]) =>
      stringTag(name, value)
    );
    return compoundPayload([
      stringTag("Name", entry.name),
      compoundTag("Properties", propertyTags)
    ]);
  });
  const blockList = blocks.map(({ pos, state }) => compoundPayload([
    listTag("pos", 3, pos.map(i32)),
    intTag("state", state)
  ]));

  return compoundTag("", [
    intTag("DataVersion", DATA_VERSION),
    listTag("size", 3, sizeList),
    listTag("palette", 10, paletteList),
    listTag("blocks", 10, blockList),
    listTag("entities", 10, [])
  ]);
}

mkdirSync(OUTPUT, { recursive: true });
for (let seed = 0; seed < 3; seed++) {
  const name = `crater_${String.fromCharCode(97 + seed)}.nbt`;
  const blocks = generate(seed);
  const spikeCount = variantSpikes(seed).length + proceduralSpikes(seed).length;
  writeFileSync(resolve(OUTPUT, name), gzipSync(structureNbt(blocks), { level: 9 }));
  console.log(`${name}: ${blocks.length} blocks, ${spikeCount} crystal spikes`);
}

mkdirSync(SURFACE_OUTPUT, { recursive: true });
for (let seed = 0; seed < 3; seed++) {
  const name = `spire_${String.fromCharCode(97 + seed)}.nbt`;
  const spires = surfaceSpireVariants(seed);
  const blocks = generateSurfaceSpire(seed, SURFACE_SIZE, spires);
  writeFileSync(
    resolve(SURFACE_OUTPUT, name),
    gzipSync(structureNbt(blocks, SURFACE_SIZE), { level: 9 })
  );
  console.log(`${name}: ${blocks.length} blocks, ${spires.length} giant crystals`);
}

mkdirSync(OUTSKIRT_OUTPUT, { recursive: true });
for (let seed = 0; seed < 3; seed++) {
  const name = `outskirt_${String.fromCharCode(97 + seed)}.nbt`;
  const spires = outskirtSpireVariants(seed);
  const blocks = generateSurfaceSpire(seed, OUTSKIRT_SIZE, spires, 1009);
  writeFileSync(
    resolve(OUTSKIRT_OUTPUT, name),
    gzipSync(structureNbt(blocks, OUTSKIRT_SIZE), { level: 9 })
  );
  console.log(`${name}: ${blocks.length} blocks, ${spires.length} outskirt crystals`);
}

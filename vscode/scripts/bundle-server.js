// Copies the language server built by './gradlew :lsp:installDist' and the
// license into the extension folder, so that 'vsce package' includes them.
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const dist = path.join(root, '..', 'lsp', 'build', 'install', 'minicpp-lsp');
if (!fs.existsSync(path.join(dist, 'lib'))) {
  console.error(`${dist} not found; run './gradlew :lsp:installDist' in the repository root first`);
  process.exit(1);
}

const target = path.join(root, 'server');
fs.rmSync(target, { recursive: true, force: true });
fs.cpSync(path.join(dist, 'lib'), path.join(target, 'lib'), { recursive: true });
fs.copyFileSync(path.join(root, '..', 'LICENSE'), path.join(root, 'LICENSE'));
console.log(`bundled ${fs.readdirSync(path.join(target, 'lib')).length} server jars into ${target}`);

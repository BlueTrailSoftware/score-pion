const fs = require('fs');
const pkg = require('./package.json');
const envFiles = [
  './src/environments/environment.ts',
  './src/environments/environment.prod.ts',
];

envFiles.forEach((envPath) => {
  let envContent = fs.readFileSync(envPath, 'utf8');
  // Replace version property or add if missing
  if (/version:\s*'[^']*'/.test(envContent)) {
    envContent = envContent.replace(
      /version:\s*'[^']*'/,
      `version: '${pkg.version}'`,
    );
  } else {
    envContent = envContent.replace(
      /(googleSSOCallbackPath:.*?)(\n)/,
      `$1,\n  version: '${pkg.version}'$2`,
    );
  }
  fs.writeFileSync(envPath, envContent);
  console.log(`Updated ${envPath} version to ${pkg.version}`);
});

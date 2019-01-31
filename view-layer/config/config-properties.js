const { resolve, join } = require('path');
const { realpathSync } = require('fs');

const appDirectory = realpathSync(join(__dirname, './..'));
const resolveApp = relativePath => resolve(appDirectory, relativePath);

process.env.NODE_CONFIG_DIR 
  = resolveApp('src/main/resources');
const config = require('node-config-simple')

module.exports = {
  viewLayerDevServerPort: config.get('web.view-layer.port'),
  viewLayerPublicPath: resolveApp(config.get('web.view-layer.public-path')),
  viewLayerContentBase: resolveApp(config.get('web.view-layer.content-base')),
  viewLayerNodeModules: resolveApp('node_modules'),
viewLayerParentNodeModules: resolveApp('../node_modules'),
  viewLayerUseKotlin: config.get('web.view-layer.use-kotlin'),
  viewLayerKotlinEntry: resolveApp(config.get('web.view-layer.kotlin-entry')),
  viewLayerJsEntry: resolveApp(config.get('web.view-layer.js-entry')),
  viewLayerKotlinOut: resolveApp('node_modules/.cache/kotlin-webpack'),
  default: resolveApp,
  appDirectory,
  resolveApp
};

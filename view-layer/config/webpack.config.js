'use strict';

const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const WatchMissingNodeModulesPlugin = require('react-dev-utils/WatchMissingNodeModulesPlugin');
const KotlinWebpackPlugin = require('@jetbrains/kotlin-webpack-plugin');

const fontFilesLoader = require('./loaders-by-asset/fonts');
const jsLoader = require('./loaders-by-asset/jsx');
const imagesLoader = require('./loaders-by-asset/images');

const webpackAliases = require('./webpack.config.aliases');
const config = require('./config-properties');

const kotlinModuleName = 'viewLayerKotlinApp';
const resolveModules = getResolveModules();
const moduleRules = getModuleRules();
const plugins = getPlugins();

const devWebpackConfig = {
  mode: 'development',
  devtool: 'cheap-module-eval-source-map',
  devServer: {
    port: config.viewLayerDevServerPort,
    hotOnly: true,
    inline: true,
    publicPath: config.viewLayerPublicPath,
    contentBase: config.viewLayerContentBase,
    historyApiFallback: true, // allows client side routing
  },
  entry: [
    'babel-polyfill',
    config.viewLayerUseKotlin 
      ? kotlinModuleName
      : config.viewLayerJsEntry,
  ],
  resolveLoader: {
    modules: resolveModules,
  },
  resolve: {
    alias: {
      ...webpackAliases(config),
    },
    modules: resolveModules
  },
  module: {
    strictExportPresence: true,
    rules: moduleRules
  },
  plugins,
}

function getResolveModules() {
  const retVal = [
    config.viewLayerNodeModules,
  ];

  if (config.viewLayerUseKotlin) {
    retVal.push(config.viewLayerKotlinOut)
  }

  return retVal;
}

function getModuleRules() {
  const retVal = [
    ...fontFilesLoader,
    ...imagesLoader,
  ];

  if (config.viewLayerUseKotlin) {
    const { getJsKotlinSourceMapLoader } = jsLoader;
    retVal.push(...getJsKotlinSourceMapLoader(config));
  } else {
    const { getJsLoader } = jsLoader;
    retVal.push(...getJsLoader(config))
  }

  return retVal;
}

function getPlugins() {
  const retVal = [
    // needed for HMR
    new webpack.NamedModulesPlugin(),
    new webpack.HotModuleReplacementPlugin(),
    new HtmlWebpackPlugin(),
    new webpack.HotModuleReplacementPlugin(),
    // new WatchMissingNodeModulesPlugin(config.viewLayerNodeModules), 
  ];

  if (config.viewLayerUseKotlin) {
    retVal.unshift(new KotlinWebpackPlugin({
      src: config.viewLayerKotlinEntry,
      output: config.viewLayerKotlinOut,
      moduleName: kotlinModuleName,
      librariesAutoLookup: true
    }));
  }

  return retVal;
}

module.exports = devWebpackConfig;

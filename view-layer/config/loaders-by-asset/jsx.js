const jsLoader = [{
  test: /\.js$/,
  loader: 'babel-loader',
  options: {
    cacheDirectory: true,
    plugins: [
    ],
    presets: [
      "@babel/preset-react", "@babel/preset-env"
    ],
  },
}]

const getJsKotlinSourceMapLoader = (config) => [{
  test: /\.js$/,
  include: config.viewLayerKotlinOut,
  loader: 'source-map-loader',
  enforce: 'pre',
}]

module.exports = {
  getJsKotlinSourceMapLoader,
  getJsLoader: () => jsLoader
};

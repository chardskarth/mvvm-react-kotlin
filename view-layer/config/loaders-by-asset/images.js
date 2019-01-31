module.exports = [{
  test: /\.(?:ico|gif|png|jpg|jpeg|webp|svg)$/i,
  use: [
    {
      loader: 'file-loader',
      options: {
        hash: 'sha512',
        digest: 'hex',
        name: 'assets/[hash].[ext]',
      },
    },
    {
      loader: 'image-webpack-loader',
      options: {
        mozjpeg: {
          progressive: true,
        },
        gifsicle: {
          interlaced: true,
        },
        optipng: {
          optimizationLevel: 7,
        },
        pngquant: {
          quality: '65-90',
          speed: 4,
        },
      },
    },
  ],
}];

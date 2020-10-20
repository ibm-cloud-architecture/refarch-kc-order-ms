module.exports = {
  devServer: {
    proxy: {
      '^/orders': {
        target: 'http://localhost:9080',
        changeOrigin: true
      },
    },
    port: 4545
  },
  "transpileDependencies": [
    "vuetify"
  ]
}
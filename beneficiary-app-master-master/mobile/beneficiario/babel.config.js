module.exports = function(api) {
  api.cache(true);
  return {
    presets: [
      [
        'babel-preset-expo',
        {
          web: {
            disableImportExportTransform: true
          }
        }
      ]
    ],
    env: {
      production: {
        plugins: ['react-native-paper/babel']
      }
    }
  };
};

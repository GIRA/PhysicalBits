
let i18n = (function () {
    let spec = [],
        locales = {},
	      availableLocales = [],
        current = "en",
        untranslatable = [];

    let observers = {
      "change" : [],
      "update" : []
    };

    function init(translations) {
        // Spec
        spec = translations[0];
        locales = {};
        untranslatable = [];

        // Create locales according to spec;
        for (let i = 0; i < spec.length; i++) {
          locales[spec[i]] = {};
          availableLocales.push(spec[i]);
        }

        for (let i = 1; i < translations.length; i++) {
            let row = translations[i];
            for (let j = 0; j < row.length; j++) {
                let locale = locales[spec[j]];
                locale[row[0]] = row[j];
            }
        }

        chooseCurrentLocale();
    }

    function chooseCurrentLocale() {
      let preferredLanguage  = "en"; // Default

      let userLanguage = localStorage["i18n.userLanguage"];
      if (userLanguage) {
        preferredLanguage = userLanguage;
      } else {
        let navigatorLanguages = getNavigatorLanguages();
        for (let i = 0; i < navigatorLanguages.length; i++) {
          let languageCode = navigatorLanguages[i];
          if (availableLocales.includes(languageCode)) {
            preferredLanguage = languageCode;
            break;
          }
        }
      }

      currentLocale(preferredLanguage);
    }

    function getNavigatorLanguages() {
      if (navigator.languages && navigator.languages.length) {
        return navigator.languages;
      } else {
        return [navigator.userLanguage || navigator.language || navigator.browserLanguage];
      }
    }

    function on (evt, callback) {
      observers[evt].push(callback);
    }

    function trigger(evt) {
      observers[evt].forEach(function (fn) {
        try {
          fn();
        } catch (err) {
          console.log(err);
        }
      });
    }

    function translate (string) {
        let locale = locales[current] || {};

        if (locale.hasOwnProperty(string)) {
            let translation = locale[string] || string;
            return translation.toString();
        } else {
            if (untranslatable.indexOf(string) === -1) untranslatable.push(string);
            return string.toString();
        }
    }

    function currentLocale (newLocale) {
        if (newLocale === undefined) return current;

        current = localStorage["i18n.userLanguage"] = newLocale;
        trigger("change");
        updateUI();
    }

    /**
     * If the 'node' parameter is defined it will only search inside the node.
     * Otherwise it will search across the whole page.
     */
    function updateUI (node) {

      (node === undefined ?
          $("[lang]") :
          $(node).find("[lang]").addBack("[lang]"))
              .each(function () {
                  let $node = $(this);
                  if (this.nodeName === "INPUT") {
                      if (this.type === "button"
                          || this.type === "submit") {
                          update($node, $node.val, $node.val);
                      } else if (this.type === "option") {
                          update($node, $node.text, $node.text);
                      } else if (this.type === "text") {
                          if (this.placeholder === "") {
                              update($node, $node.val, $node.val);
                          } else {
                              // Take care of the placeholder, if any
                              update($node,
                                  function () {
                                      return $node.attr("placeholder");
                                  },
                                  function (val) {
                                      $node.attr("placeholder", val);
                                  }
                              );
                          }
                      }
                  } else {
                      update($node, $node.text, $node.text);
                  }
              });
      (node === undefined ?
          $("[lang-fn]") :
          $(node).find("[lang-fn]").addBack("[lang-fn]"))
              .each(function () {
                  let $node = $(this);
                  this[$node.attr("lang-fn")](this);
              });

      trigger("update");
    }

    function update($node, getter, setter) {
        let node = $node.get(0);
        if (node.nodeName === "HTML") return;
        if (node.original === undefined) {
            node.original = getter.call($node);
        }
        let original = node.original;
        let translated = i18n.translate(original);
        setter.call($node, translated);
    }

    String.prototype.translated = function () {
        return i18n.translate(this);
    }

    return {
        currentLocale: currentLocale,
        availableLocales: availableLocales,
        init: init,
        translate: translate,
        updateUI: updateUI,
        on: on,

        untranslatable: function () { return untranslatable; }
    };
})();

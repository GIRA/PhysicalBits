
var i18n = (function () {
    var spec = [],
        locales = {},
        current = "en",
        untranslatable = [];

    function init(translations) {
        // Spec
        spec = translations[0];
        locales = {};

        var i;

        // Create locales according to spec;
        for (i = 0; i < spec.length; i++) {
            locales[spec[i]] = {};
        }

        for (i = 1; i < translations.length; i++) {
            var row = translations[i];
            for (var j = 0; j < row.length; j++) {
                var locale = locales[spec[j]];
                locale[row[0]] = row[j];
            }
        }
    }

    function translate (string) {
        var locale = locales[current] || {};

        if (locale.hasOwnProperty(string)) {
            return locale[string].toString();
        } else {
            if (untranslatable.indexOf(string) === -1) untranslatable.push(string);
            return string.toString();
        }
    }

    function currentLocale (newLocale) {
        if (newLocale === undefined) return current;
        current = newLocale;
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
                    var $node = $(this);
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
                    var $node = $(this);
                    this[$node.attr("lang-fn")](this);
                });
    }

    function update($node, getter, setter) {
        var node = $node.get(0);
        if (node.nodeName === "HTML") return;
        if (node.original === undefined) {
            node.original = getter.call($node);
        }
        var original = node.original;
        var translated = i18n.translate(original);
        setter.call($node, translated);
    }

    String.prototype.translated = function () {
        return i18n.translate(this);
    }

    return {
        currentLocale: currentLocale,
        init: init,
        translate: translate,
        updateUI: updateUI,

        untranslatable: untranslatable
    };
})();

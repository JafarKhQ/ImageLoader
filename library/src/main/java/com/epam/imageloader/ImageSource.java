package com.epam.imageloader;


enum ImageSource {
    WEB, LOCAL, UNKNOWN;

    private static final String[] PREFIX_WEB =
            {
                    "http://",
                    "https://"
            };

    private static final String[] PREFIX_LOCAL =
            {
                    "file:///",
                    "/"
            };

    public static ImageSource getImageSource(String where) {
        final String lowerWhere = where.toLowerCase();
        if (isSourceFrom(lowerWhere, PREFIX_WEB)) {
            return WEB;
        } else if (isSourceFrom(lowerWhere, PREFIX_LOCAL)) {
            return LOCAL;
        } else {
            return UNKNOWN;
        }
    }

    private static boolean isSourceFrom(String where, String[] prefixs) {
        for (String prefix : prefixs) {
            if (where.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }
}

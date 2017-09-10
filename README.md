Android Flux
============

[![Release](https://img.shields.io/badge/latest%20release-1.0.0-green.svg)](https://github.com/brachior/android-flux/releases/tag/v1.0.0)
[![Download](https://api.bintray.com/packages/brachior/android/flux/images/download.svg)](https://bintray.com/brachior/android/flux/_latestVersion)
[![License](http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png)](http://www.wtfpl.net/)

![demo](https://raw.githubusercontent.com/brachior/android-flux/master/demo.gif)

### Usage

Add this to your app build.gradle:

```gradle
dependencies {
    compile 'net.brach.android:flux:1.0.0'
}
```

##### ➫ JAVA

No need to initialized.
Use the _Flux.Builder_ to pre-calculate the animation.
Then call the _'start()'_ function to run the animation.

```java
final Flux flux = new Flux.Builder(this)
        .from(button)
        .to(to)
        .number(1 + random.nextInt(100))
        .assets(bitmaps, 15, 65)
        .duration(1000, 1500)
        .build();

button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        flux.start();
        v.setOnClickListener(null);
    }
});
```

### Contribution

Feel free to create pull requests / issues

### Licence

This project is licensed under the WTFPL (Do What The Fuck You Want To Public License, Version 2)

[![WTFPL](http://www.wtfpl.net/wp-content/uploads/2012/12/logo-220x1601.png)](http://www.wtfpl.net/)

Copyright © 2017 brachior [brachior@gmail.com](mailto:brachior@gmail.com)

This work is free. You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by Sam Hocevar. See the COPYING file for more details.

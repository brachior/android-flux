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

You have to initiate the FluxBuilder calling 'attach2Activity':

```java
FluxBuilder.attach2Activity(this);
```

After you can call a flux:

```java
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // using bitmaps
        FluxBuilder.flux(this, button, to, 55, bitmaps, 20, 70, 1000, 1500);

        // using circles
        FluxBuilder.flux(this, button, to, 55, 15, 20, 1000, 1500);
    }
});
```

You can pre-calculate the animation calling 'make'.

```java
final Flux flux = FluxBuilder.make(this, button, to, 55, 15, 20, 1000, 1500);
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

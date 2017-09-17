Android Flux
============

[![Release][3]][1]
[![Download][4]][2]
[![License][6]][5]

![demo][9]

### Usage

##### ➫ GRADLE

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
        .from(viewFrom)
        .to(viewTo)
        .number(400)
        .assets(bitmaps, 15, 65)
        .duration(1000, 1500)
        .build();

viewFrom.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        flux.start();
        v.setOnClickListener(null);
    }
});
```

You can also run the animation directly (be careful, it may take time) using the _'run()'_ function.

```
new Flux.Builder(this)
        .from(view)
        .to(to)
        .number(400)
        .assets(bitmaps, 15, 65)
        .duration(1000, 1500)
        .run();
```

### Classes

#### ➫ _Flux.Builder_

To create a _Flux_ animation, you should used the _Flux.Builder_ class.

##### Methods

These methods can be chained:

* _**number**(int count)_: (required)  
The number of particles you want to animate.
* _**from**(View from)_: (required)  
The view where the particles will start.
* _**to**(View to)_: (required)  
The view where the particles will arrive.
* _**duration**(int min, int max)_: (required)  
The interval of durations in milliseconds.
* _**circle**(float radiusMin, float radiusMax)_: (required if _assets_ isn't called)  
The interval of circle radius in pixels.
* _**assets**(List<String> assets, int sizeMin, int sizeMax)_: (required if _circle_ isn't called)  
The list of assets and the interval of sizes in pixels.
* _**addInterpolators**(TimeInterpolator... interpolators)_:  
Add multiple interpolators.
An interpolator will be chosen randomly in this list to be associate to a particle.
* _**removeInterpolators**(TimeInterpolator... interpolators)_:  
Remove multiple interpolators.
If the list is empty, the default interpolators will be added.
* _**clearInterpolators**()_:  
Remove all interpolators.
If the list is empty, the default interpolators will be added.

These methods build or run the animation:

* _**run**()_:  
Immediately execute the animation when it will be created.
* _**build**()_:  
Returns a _Flux_ which is the pre-calculation of the animation.

#### ➫ _Flux_

The _Flux_ class is the animation.

##### Methods

The _Flux_ class behaves like an _[android.animation.Animator][10]_.
You can add or remove listeners,
you can start, cancel, end, pause or resume the animation
and retrieve some information about it.

An additional method, called _**remove**()_ is added to delete all views created by the animation.

### Contribution

Feel free to create pull requests / issues

### Licence

This project is licensed under the WTFPL (Do What The Fuck You Want To Public License, Version 2)

[![WTFPL][7]][5]

Copyright © 2017 brachior [brachior@gmail.com][8]

This work is free. You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by Sam Hocevar. See the COPYING file for more details.

[1]: https://github.com/brachior/android-flux/releases/tag/v1.1.0
[2]: https://bintray.com/brachior/android/flux/_latestVersion

[3]: https://img.shields.io/badge/latest%20release-1.1.0-green.svg
[4]: https://api.bintray.com/packages/brachior/android/flux/images/download.svg

[5]: http://www.wtfpl.net/
[6]: http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png
[7]: http://www.wtfpl.net/wp-content/uploads/2012/12/logo-220x1601.png
[8]: mailto:brachior@gmail.com

[9]: https://raw.githubusercontent.com/brachior/android-flux/master/demo.gif
[10]: https://developer.android.com/reference/android/animation/Animator.html

[![Build Status](https://travis-ci.org/jimulabs/motion-kit.png)](https://travis-ci.org/jimulabs/motion-kit)

#Motion Kit

MotionKit is a set of libraries aiming to simplify the development of Android animations.

#Download
```
compile 'com.jimulabs.motionkit:motion-kit:0.1.0'
```
#Usage

```Java
		MirrorAnimator anim1 = $(R.id.view1).scale(0, 3, 1)
			.interpolator(android.R.interpolator.bounce)
			.duration(1000);
		MirrorAnimator anim2 = $(R.id.view2).alpha(0, 1);
		// choreograph animators
		sequence(anim1, anim2).start();
		// or populate your views with mock data
		ChartView chart = (ChartView)$(R.id.chart).getView();
		chart.setData(createSomeMockData());
```

See [examples](https://github.com/jimulabs/motion-kit/tree/master/examples) for details.

#License
```
Copyright 2015 jimu Labs Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
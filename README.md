# SpeedWidget

## Introduction
The SpeedWidget is a Android widget that shows current speed on home page in real time.

## Structure
**SpeedService** --It implements IBaseGpsListener to get current speed.

**SpeedAppWidget** -- It start the SpeedService as foreground service, and update imgage and text based on the current information.

## Preview
![image](https://user-images.githubusercontent.com/45302788/172545552-b6fbcc0a-00d3-443b-ac19-34edfcc78cf2.png)

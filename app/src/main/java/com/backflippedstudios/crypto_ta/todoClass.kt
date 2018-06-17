package com.backflippedstudios.crypto_ta

/**
 * Created by C0rbin on 12/6/2017.
 */
class todoClass {

    //TODO - error, when flipping screen, adds extra volumn graph
    //TODO: Update overlays to version 2.0:
    //      1. DONE 5/28/2018 Have internal dropdowns
    //      2. DONE 6/3/2018 Sliders
    //      3. DONE 6/5/2018 - major - Figure out how to update the edit text faster
    //      3a.TODO Color picker,
    //          DONE 6/14/2018 adding color box,
    //          DONE 6/15/2018 adding click listner to pop up color picker,
    //          Done 6/16/2018 adding change color line on color picker change
    //          DONE 6/16/2018 update preferences for line color
    //          TODO color picker for when an item doesnt have a detailed list
    //          TODO update lib to when selecting the color, set the position aswell. This is for defaults if they exist on the graph
    //          TODO update lib to have reset default button
    //          TODO Fix slider positions
    //          TODO, 2nd slider not updating graph
    //      3b.TODO simplify to have 1 location for values on Overlay
    //      4.Maybe enhancement?- fix data values syncing on edit text
    //      DONE. Dont fill the screen espically on landscape
    //      DONE. Syncing both parent and detailed items to an extent
    //      DONE 5/28/2018  Button on parent to activate the detailed items
    //      DONE 5/28/2018 switching out checkboxes with switch
    //      DONE 6/2/2018 syncing up on click listners
    //      DONE 6/5/2018 - MAde the overlay RV move from the right of the screen
    //      Done 6/13/2018 - maybe force overlay recycler view to limit number of displayed items. Forced to 550 dpi
    //          This will make for when collapsing the view with some deailed items visible on the next
    //          reopen to expand the view. When collapsing the items now on the 2nd reopen it causes
    //          extra items to be empty and looks bad.

    //TODO: Landscape has not all charts can fit without having scrolling. Only on 4 graphs

    //TODO: Issue when scrolling on 1 diagram and move the other, the diagrams get out of sync on the x-axis

    //TODO-LOW: Indicators I am interested in adding -
    //      TODO: Pivot Points, Aroon, Awesome Oscillator, Chande Momentum Oscillator,
    //      TODO: Coppock Curve, Detrend Price Oscillator, Fisher Transform, Hull Moving Average,
    //      TODO: Trailing Stop Loss, Triple EMA, Williams %R.


    //TODO-4: add tabs
    //          TODO: Tab for cards comparing daily changes kinda like main cryptowat.ch
    //          https://api.cryptowat.ch/markets/EXCHANGE/COIN_PAIR/summary

    //TODO - Custom settings
    //      1. Support having 1 handed UX, could mean haveing multiple xmls

    //TODO - Look at CPU usages on cryptowatch api

    //DONE 5/26/2018: Make graphs more generic
    //      1a(DONE). Making OverlayAdapter more abstract.
    //      1b. For OverlayAdapter -  Adding 2nd viewholder type which will include slider to value, color to line color
    //      2.(DONE 5//26/2018) - Investigate chartlist Adapter to be more generic

    //DONE 5/25/2018 - Make checkbox more generic when on Overlay adapter
    //DONE 5/25/2018: When ichcloud is added when other graphs are not visible, we dont add in the extra spots
    // DONE 5/25/2018  This causes an issue when making the graphs visible that they are not aligned correctly

    //DONE 5/22/2018: Orientation change on phone device is too sensitive
    //DONE 5/22/2018: Inital graph has the border padding around the outside
    //Done 5/22/2018: Aroon Up Down indicator

    //Done 5/18/2018- Make adding new graphs generic and expandable

    //DONE 5/17/2018 - Add Aroon oscillator chart http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:aroon_oscillator

    //DONE 5/15/2018- remove Crypto-TA Action bar
    //DONE 5/15/2018- Fix the bottom clipping now that our menu bar is smaller.
    //DONE 5/15/2018 - Add year to date lable for 12H,1D,3D,1W periods

    //Done 5/14/2018: Space between graphs is pretty big and a WASTE of space & Any recycler view items

    //Done 5/13/2018: Have drawer animation to convert to an arrow and back

    //Done 5/12/2018: Add current value in USD(or desired currency)

    //DONE 5/11/2018: Remove indicator button since we are going the drawer button insead
    //Done 5/11/2018: When coin fails, disable the overlay indicators, stop loading animation
    //Done 5/11/2018: Check if volume is actually selected before we update Ichimoku_Cloud

    //DONE 5/6/2018 - Color drawer button

    //Didnt work 5/6/2018 - attempt changing menu to be constraint layout insead of Linear Layout to see that this improves spacing

    //DONE 5/6/2018: Add current price that updates every 5 seconds API: https://api.cryptowat.ch/markets/gdax/btcusd/summary
    //      https://api.cryptowat.ch/markets/EXCHANGE/COIN_PAIR/summary
    //      DONE 5/6/2018: Turn price green when live price goes up, red when it goes down

    //Done 5/4/2018 - move the menu_header_view at the bottom of the screen
    //      -Issue on getting menu header view to show at the bottom since margins are not working

    //DONE 5/2/2018 Figure out how to have the indicators recycler view fit width to content

    //DONE 4/30/2018 Fix Keltner Channel when first few items are infinate

    //DONE 4/29/2018: Adding colors for the volume bars

    //DONE 4/28/2018: maybe enable/disable volume graph
    //DONE 4/28/2018: issue where graph has leftover points when UpdateCandlestick added stuff to TS

    //DONE 4/27/2018: Add time stamp on X-Axis and make it on the bottom
    //      DONE 4/27/2018: Issue where x-axis time/date didnt shot up on initial candle sticks
    // This looks like XAxisRenderer might be the place start looking
    //      DONE 4/27/2018:: Here is how adding spcific lables might help: https://github.com/PhilJay/MPAndroidChart/pull/2692/files
    //      DONE 4/27/2018: Support other times other than the 1m
    //      Overcome now since we have month-day: Add in the Day marker
    //      Overcome now since we have month-day: Maybe have day marker a different color is possible
    //      DONE 4/27/2018: Format minitue to 2 decimal places
    //      DONE 4/27/2018:: Format time to 12H time period

    //Done 4/24/2018 Custom legend to be on the graph. This consist of having the legend inside the graph, top left, 1 item per line
    //Done 4/24/2018      Remove refresh button
    // DONE 4/24/2018 add pull down to refresh
    //Done 4/23/2018 Support better landscape mode
    //      Done 4/23/2018 When switching orientation, valueIndex is lost
    //      Done 4/23/2018 When reloading make sure gestures are reset. Looks like Indicators seems to be turned on
    //      Done 4/20/2018 - Fixed issue when screen is rotated that screen loads 4 times due to "changed" spinners
    //      Done 4/21/2018: Fix the initial zoom
    //      Done 4/21/2018: Fix some crashes when trying to do to many things a once. Dissabled spinners when graph is loading
    //      Done 4/22/2018: Fixed selection of ETH dropdown because ETC had ETH in the detailed label

    //4/15/2018: Add volume bar graph
    //04_08_2018: Figure out how to get softkey on indicators edits. 3/31 - might be an issue with editText in a listview IDK why it oesnt work
    //DONE: Add source for Binance coins
    //DONE 12_6_2017_pm: REmove y-axis on left side

    //DONE 12_6_2017_pm: Fast loads when swapping coins/exchange/base/timeperiod when Technical Analysis is running
    //Done 12_7_2017: Fix legend to wrap when there are many indicators
    //Done 12_7_2017: remember which TA Indicators were last enabled when swapping C/E/B/T
    //Done 12_7_2017: Remember TA indicators on restart
    //Done 12_7_2017: Add loading to be yellow text


}
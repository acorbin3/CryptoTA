package com.backflippedstudios.crypto_ta

/**
 * Created by C0rbin on 12/6/2017.
 */
class todoClass {




    //TODO: Indicators I am interested in adding -
    //      TODO: MACD, MassIndex,RandomWalkIndex,RAVI,  Trailing Stop Loss
    //TODO - Update screenshots on play store

    //TODO - Add splash screen with loading

    //TODO - feedback button

    // TODO - BUG - When PPO long term is greater than short term this will throw an error
    //    java.lang.IllegalArgumentException: Long term period count must be greater than short term period count
    //    at org.ta4j.core.indicators.PPOIndicator.<init>(PPOIndicator.java:41)

    // Release 2

    //TODO-4: add tabs
    //          TODO: Tab for cards comparing daily changes kinda like main cryptowat.ch
    //          https://api.cryptowat.ch/markets/EXCHANGE/COIN_PAIR/summary

    //TODO - Custom settings
    //      1. Support having 1 handed UX, could mean haveing multiple xmls

    //TODO - Look at CPU usages on cryptowatch api
    //TODO - Look into moving legend to bottom left??



    //Release 1.3.13 10/31/2018
    //DONE 10/25/2018 - Fibonacci Reversal indicator
    //DONE 10/26/2018 - DeMark Pivot Point
    // DONE 10/26/2018 - BB Width
    // DONE 10/26/2018 - BB % B

    //DONE 10/31/2018 - ADX, +DI -DI
    // DONE 10/31/2018 - Fix limit line
    // DONE 10/31/2018 - Fix on value select to display values
    //DONE 10/31/2018- Fix when switching a line on and off, do not recalculate for SEPARATE charts
    //      DONE 10/31/2018: CCI,FIx Chandelier for both short and long, Double EMA, Fisher Transform,
    //DONE 10/31/2018 - Bug - Issue when resetting default color

    // Release 1.3.12 - 10/24/2018
    //DONE 10/24/2018 - Fixed loading menu bug
    // DONE 10/24/2018  - when background indicators are loading and user attempts to turn on, notify still loading


    //Release 1.3.11
    //DONE - Add warning that indicator cant be added becuase too many charts
    //DONE 9/30/2018 - Awesome Oscillator
    //DONE 9/30/2018 - Rate Of Change

    //DONE 10/2/2018 - Fix simplification on Ich Cloud for seperate charts, Coppock Curve, Chande Momentum Oscillator,
    //DONE 10/3/2018 - Williams R, Tripple EMA, Ulser Index

    //DONE 10/4/2018 - Bug - Why does when changing the values like timeFrame not work?
    //DONE 10/8/2018 - BUG - When changing exchanges, the overlays do not appear

    //DONE 10/9/2018 - Added Positive & Negative volume indicators, On Balance Power Indicator
    //               - Attempted to add in Piviot points

    //DONE 10/23/2018 Piviot points
    // DONE 10/23/2018 - Fix default legeond
    // DONE 10/23/2018 - Fix on click legond

    //DONE 10/23/2018  - enhancement - When wanting to reload, there is a thread running in the background
    // DONE 10/23/2018  calculating the non selected indicatiors, that needs to be killed.

    // 1.3.2-1.3.7
    // DONE 9/18/2018: Made initial loading faster by storing the coins locally
    //DONE: Look into possibly simplifying seperate charts

    // DONE 9/20/2018: computed, then go ahead and recalculate ones that have not been selected.
    // DONE : Detrend Price Oscillator
    // DONE 9/20/2018 - Hull Moving Average
    // DONE  - ZLEMA
    // DONE - Volume Weighted Average Price
    // DONE 9/27/2018 - Moving volume weighted average price

    // 1.3.1
    //8/22/2018: Testing - Fix regression testing

    // DONE 8/24/2018- simplify the chartlist adapter legedon and adding lines
    //DONE 8/24/2018 - Check if simplification has color change issues?


    //8/24/2018 - ISsue for horizontal screen
    //DONE 8/25/2018 - the last seperate chat is not filled in the screen
// DONE 8/25/2018 - Issue on Stoch Osci when adding Ich cloud, the graph doesnt get shifted
    //      DONE 8/25/2018: PPO

    //1.3
    //      DONE 8/14/2018: DetrendStocastic Oscillator Price Oscliator
    //      DONE 8/14/2018:  D & K
    //DONE 8/20/2018- only calculate overlay that is selected. Then once all selected have been
    //DONE 8/20/2018 - Fix the candlesicks that go away after overlays get loaded

    //1.2.2
    //DONE 8/10/2018: RSI Indicator
    // DONE: 8/11/2018 Fix issues with scrolling, and click indicator

    //Done 8/3/2018 - Add cloud messaging or push messaging
    // DONE 8/4/2018(Didnt implement due to FireBase console limitations) - Filter out could message if they have the latest version
    //  Done 8/4/2018 - Add settings so that someone can turn off messaging

    //DONE 8/5/2018 - ISSUE = when reloading app, it selects the correct first coin but always picks the first one in the list,so the 2nd coin could be incorrect.
    //DONE 8/5/2018  - ISSUE = Crash when slecting coins with multiple slashes
    //DONE 8/7/2018 - regression testing - Looping over all the coins to just make sure they load
    //DONE 8/7/2018 - Check crashes from crashanalitics
    //DONE 8/8/2018      Have service to daily look at cryptowatch's assets to update the firebase DB

    // Done 8/1/2018 - save off all the coin pair data so loading doesnt take a long time each start up
    // Done 8/1/2018     Insead of looking at cryptowatch api, look at firebase DB for info..
    //DONE 7/29/2018 - Combine the coin pairs into 1 dropdown menu

    //DONE 7/5/2018 - Firebase analytics
    //DONE 7/5/2018 -- UUID for user
    //DONE 7/26/2018 - Added date on legend when clicking on the graph

    //DONE 7/4/2018 - Feature to share graph
    //  DONE 7/1/2018 Sharing screenshot to Social media(facebook, insta, Twitter, Snap)
    // DONE 7/1/2018 - fix permissions smoothness
    //  Done 7/4/2018    optimize screenshot to fit will with insta for not landscape
    //  - Upload to server and share with friends using a link using cloudinary
    // - add picker for file share or upload and link share

    // DONE 7/3/2018 - polish the instance price updater
    //DONE 7/3/2018 look at why Keltner Channel values are not shoing up on ledgeon when clicking graph.
    //DONE 7/4/2018 - Handle with internet is out

    // DONE 7/1/2018 - look into removing the soft buttons. Results - Didnt help with the screenshot
    // DONE 7/2/2018 - clipping of screenshot in landscape
    // DONE 7/2/2018 - look into increasing bottom bar text size
    //DONE 6/27/2018 - On tap of charts, show values(OHLC, values on seperate graphs at that X
    //DONE 6/28/2018 - High light on all graphs
    //DONE 6/28/2018 - ZigZag position on tap for legend
    //DONE 6/29/2018 - when a coin, or exchange chages, need to reset default legend. resetting extra
    //DONE 6/29/2018 - add the color for legend and go to the custom legend insead of extra
    //DONE 6/30/2018 - Taking a screeshot and saved it to a file & the ability to send text message or other app

    //DONE 7/1/2018 - Make sure that menu and fit all the coins sizes becuase it auto resizes

    //DONE 6/26/2018: Issue when scrolling on 1 diagram and move the other, the diagrams get out of sync on the x-axis

    //Done 6/24/2018 ISSUE - When rotating anc collapsing the RV Overlays, the text edits seems to stay around
    //Done 6/24/2018 - When the overlay RV is visible, clicking on the screen/graph collapses it.
    //Done 6/24/2018 - When refreshing and Ich Cloud is up,
    //      Done 6/24/2018 the seperate graphs end up not bring align until you turn the Ich Cloud on and off

    //DONE 6/23/2018 - error, when flipping screen, adds extra volumn graph
    //DONE 6/23/2018(some improvments, not perfect - Speed up when orentation is flipped

    //DONE 6/23/2018: Landscape has not all charts can fit without having scrolling. Only on 4 graphs

    //DONE 6/22/2018: Update overlays to version 2.0:
    //      1. DONE 5/28/2018 Have internal dropdowns
    //      2. DONE 6/3/2018 Sliders
    //      3. DONE 6/5/2018 - major - Figure out how to update the edit text faster
    //  //DONE 6/22/2018 add timeframe and color details for Exponential_MA
    //  //DONE 6/22/2018 - Issue timing issue when expanding details and seekbar have invalid initial values.
    //                      Solution was scalefator was using global which ended being from other seekbars
    //  //DONE 6/22/2018- updating the edit text from the slider directly without trying to notify a chage
    //  //DONE 6/22/2018 on the device the ok button doesnt to return for editing the values
    //      3a.DONE 6/21/2018 Color picker,
    //          DONE 6/14/2018 adding color box,
    //          DONE 6/15/2018 adding click listner to pop up color picker,
    //          Done 6/16/2018 adding change color line on color picker change
    //          DONE 6/16/2018 update preferences for line color
    //          CHANGED. This shouldnt occur since all items have at least 1 color and 1 timeframe item(2 times)
    //                      color picker for when an item doesnt have a detailed list
    //          DONE 6/21/2018 reset color but we dont care about the position the picker
    //          DONE 6/21/2018, when setting seekbar to zero, the text view and seekbar dissapear
    //          TOO_HARD update lib to when selecting the color, set the position aswell. This is for defaults if they exist on the graph
    //          TOO_HARD update lib to have reset default button
    //          DONE 6/20/2018 Fix slider positions
    //      3b.DONE 6/20/2018 simplify to have 1 location for values on Overlay
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
    //      DONE 6/21/2018, when turning off overlay, collapse the detailed items if exist



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
    //DONE:    Add source for Binance coins
    //DONE 12_6_2017_pm: REmove y-axis on left side

    //DONE 12_6_2017_pm: Fast loads when swapping coins/exchange/base/timeperiod when Technical Analysis is running
    //Done 12_7_2017: Fix legend to wrap when there are many indicators
    //Done 12_7_2017: remember which TA Indicators were last enabled when swapping C/E/B/T
    //Done 12_7_2017: Remember TA indicators on restart
    //Done 12_7_2017: Add loading to be yellow text


}
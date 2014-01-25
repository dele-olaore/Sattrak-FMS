/** USE:
<body onload="showTimeSec();"> or <body onload="showTimeMin();">
<p id="tm"></p> or <span id="tm"></span> or <div id="tm"></div> or ...
*/

function showTimeSec(){
  var tmN=new Date();
  var dH=''+tmN.getHours(); dH=dH.length<2?'0'+dH:dH;
  var dM=''+tmN.getMinutes(); dM=dM.length<2?'0'+dM:dM;
  var dS=''+tmN.getSeconds(); dS=dS.length<2?'0'+dS:dS;
  var tmp=dH+':'+dM+':'+dS;
  document.getElementById('tm').innerHTML=tmp;
  var t=setTimeout('showTimeSec()',1000);
}

function showTimeMin(){
  var tmN=new Date();
  var dH=''+tmN.getHours(); dH=dH.length<2?'0'+dH:dH;
  var dM=''+tmN.getMinutes(); dM=dM.length<2?'0'+dM:dM;
  var dS=tmN.getSeconds()%2; dS=dS==0?':':' ';
  var tmp=dH+dS+dM;
  document.getElementById('tm').innerHTML=tmp;
  var t=setTimeout('showTimeMin()',1000);
}

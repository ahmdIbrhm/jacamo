#!/bin/sh

curl http://jacamo.sourceforge.net/lv/np.gradle -s -o np.gradle \
  && gradle -b np.gradle -q

rm np.gradle
#!/bin/bash

STAT_FILE=$1
SORTED=$1"_sorted"

if [ "$#" -lt  "1" ]; then
	echo -e "Usage: \n\t $0 stats_file"
	exit 1
elif ! [ -e $STAT_FILE ]; then
	echo "$1 does not exist"
	exit 1
elif ! [ -f $STAT_FILE ]; then
	echo "$1 is not a readable file"
	exit 1
fi

sort -nr -k 2 $1 | cat -n > $SORTED

X_LABEL="Terms ordered by frequency"
Y_LABEL=("Frequency" "# of Documents" "TF" "IDF")
TITLE=("Frequency" "# of Documents" "TF" "IDF")
FILE=("freq" "n_docs" "tf" "idf")
EXTENSION=".png"

for ((i=0; i<${#Y_LABEL[@]}; ++i)); do
	gnuplot <<- EOF
		set term png
	    set xlabel "${X_LABEL}"
	    set ylabel "${Y_LABEL[i]}"
	    set title "${TITLE[i]}"
	    set key off
	    set logscale y
	    set logscale x
	    set output "${FILE[i]}${EXTENSION}"
	    plot '${SORTED}' using 0:$i+3
	EOF
done

#comment following line to keep sorted file
rm $SORTED

 
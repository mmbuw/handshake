java FeatureExtractor $1 $2
gnuplot -e "set term wxt 0 title '$1 x-axis';
			plot '$1' using 1 with lines title 'x' lt rgb 'red', 'maxima_0.txt' title 'maxima' lt rgb 'black', 'minima_0.txt' title 'minima' lt rgb 'violet', 'handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
            set term wxt 1 title '$1 y-axis';
            plot '$1' using 2 with lines title 'y' lt rgb 'green', 'maxima_1.txt' title 'maxima' lt rgb 'black', 'minima_1.txt' title 'minima' lt rgb 'violet', 'handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
            set term wxt 2 title '$1 z-axis';
            plot '$1' using 3 with lines title 'z' lt rgb 'blue', 'maxima_2.txt' title 'maxima' lt rgb 'black', 'minima_2.txt' title 'minima' lt rgb 'violet', 'handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
            pause -1"

#gnuplot -e "set term wxt 0 title '$1 y-axis';
#            plot '$1' using 2 with lines title 'y' lt rgb 'green', 'maxima_1.txt' title 'maxima' lt rgb 'black', 'minima_1.txt' title 'minima' lt rgb 'violet', 'handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
#            pause -1"
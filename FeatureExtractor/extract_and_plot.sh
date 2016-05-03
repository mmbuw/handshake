rm -rf output_data/*
java FeatureExtractor $1 $2 $3

## three axis plot
gnuplot -e "set term wxt 0 title '$1 x-axis';
			plot '$1' using 1 with lines title 'x' lt rgb 'red', 'output_data/maxima_0.txt' title 'maxima' lt rgb 'black', 'output_data/minima_0.txt' title 'minima' lt rgb 'violet', 'output_data/handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
            set term wxt 1 title '$1 y-axis';
            plot '$1' using 2 with lines title 'y' lt rgb 'green', 'output_data/maxima_1.txt' title 'maxima' lt rgb 'black', 'output_data/minima_1.txt' title 'minima' lt rgb 'violet', 'output_data/handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
            set term wxt 2 title '$1 z-axis';
            plot '$1' using 3 with lines title 'z' lt rgb 'blue', 'output_data/maxima_2.txt' title 'maxima' lt rgb 'black', 'output_data/minima_2.txt' title 'minima' lt rgb 'violet', 'output_data/handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
            pause -1"

## isolated x-plot
#gnuplot -e "set term wxt 0 title '$1 magnitudes';
#            plot '$1' using 1 with lines title 'mag' lt rgb 'cyan', 'output_data/maxima_0.txt' title 'maxima' lt rgb 'black', 'output_data/minima_0.txt' title 'minima' lt rgb 'violet', 'output_data/handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
#            pause -1"

## isolated y-plot
#gnuplot -e "set term wxt 0 title '$1 y-axis';
#            plot '$1' using 2 with lines title 'y' lt rgb 'green', 'output_data/maxima_1.txt' title 'maxima' lt rgb 'black', 'output_data/minima_1.txt' title 'minima' lt rgb 'violet', 'output_data/handshakes.txt' with points pointtype 5 lc rgb 'coral' title 'handshakes';
#            pause -1"
import math
import sys

def start():

    if len(sys.argv) != 3:
        print("Usage: create_magnitude_file.py input_file output_file")
        sys.exit()

    input_file_name = sys.argv[1]
    output_file_name = sys.argv[2]

    input_handle = open(input_file_name, 'r')
    output_handle = open(output_file_name, 'w+')

    for line in input_handle.readlines():
        line = line.strip()
        line = line.replace(' ', '')
        splitted_line = line.split(',')

        # the part under the square root
        discriminant = 0

        for value in splitted_line:
            parsed_float = float(value)
            discriminant += (parsed_float * parsed_float)

        output_handle.write(str(math.sqrt(discriminant)) + '\n')

    input_handle.close()
    output_handle.close()


if __name__ == '__main__':
    start()
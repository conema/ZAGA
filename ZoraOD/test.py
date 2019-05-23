import obj_detection
import tensorflow as tf
import time
import argparse


def main(_):
    parser = argparse.ArgumentParser(description='image path.')
    parser.add_argument('--image_path', help='image path')  # input parameter
    args = parser.parse_args()
    t = int(time.time())
    result = obj_detection.find_result(args.image_path, t)
    print result


if __name__ == '__main__':
    tf.app.run()
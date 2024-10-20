import whisper
import torch
import argparse

def save_model():
    parser = argparse.ArgumentParser(
        prog='Whisper model export',
        description='Export whisper model to .pth format')
    parser.add_argument(
        '--model', '-m',
        default='tiny',
        choices='tiny, tiny.en, base, base.en, small, small.en, medium, medium.en, large',
        help='whisper model size')
    parser.add_argument(
        '--version', '-v',
        action="version",
        version='%(prog)s - Version 1.0')
    args = parser.parse_args()

    model = whisper.load_model(args.model)
    torch.save(model, f"whisper_{args.model}.pth")


if __name__ == "__main__":
    save_model()

from flask import Flask, request, jsonify
from parser_engine import analyze_als
import tempfile
import os
import json

app = Flask(__name__)


@app.route("/analyze", methods=["POST"])
def analyze():
    if "file" not in request.files:
        return jsonify({"error": "ファイルがありません"}), 400

    file = request.files["file"]

    if not file.filename.endswith(".als"):
        return jsonify({"error": ".alsファイルのみ対応しています"}), 400

    with tempfile.NamedTemporaryFile(delete=False, suffix=".als") as tmp:
        file.save(tmp.name)
        tmp_path = tmp.name

    try:
        result = analyze_als(tmp_path)
        return result, 200, {"Content-Type": "application/json"}
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        os.unlink(tmp_path)


if __name__ == "__main__":
    app.run(port=5000, debug=True)

import sys
import gzip
import xml.etree.ElementTree as ET
import json
import os


def analyze_als(file_path):
    try:
        # alsファイル（gzip）を展開してXMLとして読み込む
        with gzip.open(file_path, "rb") as f:
            tree = ET.parse(f)
            root = tree.getroot()

        # プロジェクト情報の取得
        # BPMの取得（階層を深く探索）
        tempo_element = root.find(".//Tempo/Manual")
        bpm = tempo_element.get("Value") if tempo_element is not None else "120"

        tracks_data = []

        midi_count = 0
        audio_count = 0
        return_count = 0
        for track in root.findall(".//Tracks//*"):
            if track.tag == "MidiTrack":
                midi_count += 1
            elif track.tag == "AudioTrack":
                audio_count += 1
            elif track.tag == "ReturnTrack":
                return_count += 1

        # すべてのトラック（Audio/Midi/Return/Master）をループ
        for track in root.findall(".//Tracks/*"):
            # トラック名を取得
            name_element = track.find(".//Name/UserName")
            track_name = (
                name_element.get("Value")
                if name_element is not None
                else "Untitled Track"
            )

            # そのトラック内のすべてのデバイス（プラグイン）を探す
            # Abletonの標準デバイスとVSTは DeviceChain の下の特定の位置にあります
            devices = []

            # トラック内の全階層からデバイス名っぽいものを探す（強力な探索）
            # DeviceChain -> Device -> UserName またはその下の各デバイス固有のタグ
            for device in track.findall(".//DeviceChain//Devices/*"):
                # 1. ユーザーがリネームした名前
                d_name_el = device.find(".//UserName")
                if d_name_el is not None and d_name_el.get("Value"):
                    devices.append(d_name_el.get("Value"))
                else:
                    # 2. デバイス本来の名前（タグ名がデバイス名になっていることが多い）
                    # 例: <Wavetable ...> <Operator ...>
                    tag_name = device.tag
                    # 不要な接尾辞などを除外
                    if tag_name not in ["DeviceChain", "AudioToAudioDeviceChain"]:
                        devices.append(tag_name)

            # 重複を削除して保存
            tracks_data.append(
                {"name": track_name, "devices": list(dict.fromkeys(devices))}
            )

        result = {
            "project_name": os.path.basename(file_path),
            "bpm": bpm,
            "tracks": tracks_data,
            "tracks_summary": {
                "midi_count": midi_count,
                "audio_count": audio_count,
                "return_count": return_count,
            },
        }

        return json.dumps(result, ensure_ascii=False)

    except Exception as e:
        return json.dumps({"error": str(e)})


if __name__ == "__main__":
    if len(sys.argv) > 1:
        print(analyze_als(sys.argv[1]))

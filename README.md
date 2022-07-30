# modify-drop-rate

## Description
 - 모든/특정 플레이어의 블럭 및 엔티티 드랍률을 조작합니다.
 - 실행 순서: "활성화된" 개별 플레이어 설정 -> "활성화된" 글로벌 설정 -> 마인크래프트 기본값

## Commands
 - /drop clear -> 드랍률 설정 초기화 (global, player 모두)
 - /drop player clear -> 드랍률 설정 초기화 (player 만)
 - /drop (global|player \<targets>) ...
   - ... clear -> 드랍률 설정 초기화
   - ... get -> 드랍률 설정 출력
   - ... set
     - ... enabled \<value: Boolean> -> 드랍률 설정 활성화/비활성화
     - ... multiplier (all|block|entity) \<value: Int> -> 모든/블럭/엔티티 드랍률 수정

## Configuration
 - `config.json` 파일에 저장, 서버 종료 후 수정할 것을 권장합니다.

## TODO
 - 엔티티 드럅률 관련 코드 개선 (단순 드랍 배율 수정 -> 독립 실행)
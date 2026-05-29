## 1. 鏉冨▉濂戠害

- [x] 1.1 浣跨敤 `tml-docs-spec-generate` 鎶€鑳藉拰 API 妯℃澘鏇存柊 `docs/api/api-import-agent.yaml`锛屼笖璇ユ枃浠朵粎鏈嶅姟 `ApiImportAgentController`锛涙柊澧?`clarificationItems`銆乣clarificationAnswers` 鍜?SSE 绀轰緥銆?- [x] 1.2 纭涓嶉渶瑕佹洿鏂?`docs/sql/`锛屽洜涓虹幇鏈?`api_import_agent_session.plan_snapshot_json` 瀛楁鍙互瀛樺偍鏂板鐨勮鍒掑揩鐓ф暟鎹紝涓嶆秹鍙婅〃缁撴瀯鍙樻洿銆?- [x] 1.3 瀹炵幇鍓嶉槄璇诲苟閬靛畧 `docs/spec/` 涓浉鍏冲悗绔紑鍙戣鑼冿紝閲嶇偣鍏虫敞 API 濂戠害銆丏TO銆佹寔涔呭寲鍜屾棩蹇楄姹傘€?
## 2. 璇锋眰涓庡搷搴旀ā鍨?
- [x] 2.1 涓?Import Agent 璁″垝鍝嶅簲澧炲姞缁撴瀯鍖栨緞娓呴」 DTO 瀛楁锛屽悓鏃朵繚鐣?`clarificationQuestions`銆?- [x] 2.2 涓?append-turn 璇锋眰 DTO 澧炲姞鍙€夌粨鏋勫寲婢勬竻绛旀瀛楁锛屽苟鍦?API 濂戠害鍏佽鏃跺畾涔?answer-only turn 鐨勬牎楠岃鍒欍€?- [x] 2.3 鎵╁睍搴旂敤灞?command 鍜岃鍒掗鍩熸ā鍨嬶紝鍔犲叆婢勬竻椤广€侀€夐」銆佺瓟妗堛€佽緭鍏ョ被鍨嬨€乺equired 鍜?current-value 瀛楁銆?- [x] 2.4 楠岃瘉 JSON 蹇収搴忓垪鍖栦笌鍙嶅簭鍒楀寲鍙互鍦ㄥ凡淇濆瓨 session 涓繚鐣欐柊澧炴緞娓呭瓧娈点€?
## 3. 纭畾鎬ц鍒掔粏鍖?
- [x] 3.1 鍦ㄨ皟鐢?`ApiImportAgentPlannerPort` 涔嬪墠锛屽疄鐜版湇鍔″眰缁撴瀯鍖栫瓟妗堝簲鐢ㄣ€?- [x] 3.2 鍩轰簬褰撳墠璁″垝鏍￠獙 `clarificationId`銆乣targetPath` 鍜?`fieldKey`锛岀‘淇濊繃鏈熸垨涓嶅尮閰嶇瓟妗堣瀹夊叏鎷掔粷鎴栨爣璁颁负鏈В鍐炽€?- [x] 3.3 鏇存柊鍖垮悕璧勪骇鍖归厤閫昏緫锛屼娇鎸囧悜涓嶅畬鏁磋祫浜х殑绛旀閫氳繃 clarification id 鎴?target path 鏇存柊璇ヨ祫浜э紝鑰屼笉鏄拷鍔犻噸澶嶈祫浜с€?- [x] 3.4 褰撹姹備笉鍖呭惈 `clarificationAnswers` 鏃讹紝淇濇寔鏃х増鑷敱鏂囨湰 turn 鍙敤銆?
## 4. Planner 涓庢牎楠?
- [x] 4.1 鏇存柊 planner JSON support锛屼娇鍏惰緭鍑虹粨鏋勫寲婢勬竻椤癸紝骞剁敱杩欎簺椤规淳鐢?legacy 鏂囨湰闂銆?- [x] 4.2 涓?`assetType`銆乣authScheme`銆乣requestMethod` 鍜?`asyncTaskConfig.authMode` 绛夊瓧娈电敓鎴愭灇涓鹃€夐」銆?- [x] 4.3 璋冩暣 validator 鏂囨锛岃闈㈠悜鐢ㄦ埛鐨勬緞娓呮爣绛惧彲璇伙紝鑰屼笉鏄洿鎺ユ毚闇插悗绔瓧娈靛悕銆?- [x] 4.4 灏?staged tool-calling 璁句负宸查厤缃?planner 璺緞鐨勯粯璁ゅ€硷紝鍚屾椂淇濈暀鐜版湁鍥炴粴閰嶇疆寮€鍏炽€?
## 5. 娴嬭瘯涓庨獙璇?
- [x] 5.1 澧炲姞 controller 鎴?delegate 娴嬭瘯锛岃鐩栨柊澧炲搷搴斿瓧娈点€佺粨鏋勫寲绛旀璇锋眰銆佹棫鐗堣姹傚拰 answer-only 璇锋眰琛屼负銆?- [x] 5.2 澧炲姞 service 娴嬭瘯锛岃瘉鏄庣粨鏋勫寲绛旀鍙互娑堥櫎閲嶅缂哄け瀛楁杩介棶锛屽苟淇濇寔鏃х増鑷敱鏂囨湰鑱婂ぉ鍏煎銆?- [x] 5.3 澧炲姞 planner support 娴嬭瘯锛岃鐩栫粨鏋勫寲 item 鐢熸垚銆佹灇涓鹃€夐」銆佸尶鍚嶈祫浜ф洿鏂板拰涓嶉仐鐣欏尶鍚嶉噸澶嶉」銆?- [x] 5.4 杩愯鍚庣娴嬭瘯濂椾欢鎴栬仛鐒?Import Agent 鐨勭獎娴嬭瘯闆嗭紝骞跺湪鍙樻洿璁板綍涓鏄庡墿浣欑己鍙ｃ€?

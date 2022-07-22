# Tinder
틴더 클론코딩

# 📌 실행화면
<img src="https://user-images.githubusercontent.com/101651909/180449243-1078dd79-0690-449b-9045-e9e478abea31.gif" width="30%" height="30%">


# 📌 요구 사항
- Firebase Authenetication 사용하기
    - Email Login
    - Facebook Login
- Firebase Realtime Database 사용하기
- yuyakaido/CardStackView 사용하기

# 📌 틴더
- Firebase Authentication 을 통해 이메일 로그인과 페이스북 로그인을 할 수 있음
- Firebase Realtime Database 를 이용하여 기록을 저장하고, 불러올 수 있음
- GitHub에서 Opensource Library를 찾아 사용할 수 있음


# ✅ Facebook 로그인 & Realtime DB 사용법
https://velog.io/@dldmswo1209/Firebase-Facebook-로그인-구현-RealTime-DB-사용하기

# ✅ CardStackView 사용하기(yuyakaido)
https://github.com/yuyakaido/CardStackView#installation
깃허브 yuyakaido 라는 사람이 만든 CardStackView를 사용 했다.
그런데 implementation 하는 과정에서 아래와 같은 오류가 발생했다.
![](https://velog.velcdn.com/images/dldmswo1209/post/4f1fc72d-27ef-4ef9-ade8-a2a550169291/image.png)
# ✅ 해결 방법
1시간 동안 삽질해가며 해결 방법을 찾았지만, 해결 방법은 의외로 간단했다.
setting.graddle 에 jcenter()를 추가하면 된다.

	dependencyResolutionManagement {
      repositories {
          ...
          jcenter()
      }
    }
 
 # 📌 ListAdapter & DiffUtil 개념
## 참고자료 및 출처
   https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter

https://velog.io/@l2hyunwoo/Android-RecyclerView-DiffUtil-ListAdapter

요약 하자면 기존에 사용하던 RecyclerView의 notifyDataSetChanged()는 데이터가 변경 될 때 모든 데이터가 통째로 업데이트 되면서 지연시간이 발생하는 이슈가 있다. 
DiffUtill 은 이러한 문제를 해결하기 위해서 현재 리스트와 교체 해야할 리스트를 비교하여 실제로 바꿔야 하는 데이터만 업데이트 해서 훨씬 빠른 속도로 업데이트가 가능하다!

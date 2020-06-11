package com.visualdust.kexiebabbo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.view.children
import com.visualdust.kexiebabbo.agent.NonblockingSignAgent
import com.visualdust.kexiebabbo.agent.SignAgent
import kotlinx.android.synthetic.main.activity_attendance.*
import java.util.function.Consumer
import com.visualdust.kexiebabbo.data.Resources as VDR


class ActivityAttendance : AppCompatActivity() {
//    private val agent = SignAgent.getAgent()
    private val nbAgent = NonblockingSignAgent.getAgent()
    private var logable = true
    private var logedin = false
    private lateinit var parent: ConstraintLayout
    private lateinit var animBt: Button
    private lateinit var timer: TextView
    private lateinit var attendanceListGridLayout: GridLayout
    private lateinit var topFiveListLinearLayout: LinearLayout
    private lateinit var timeDesrip: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomBar: LinearLayout
    private lateinit var clipboardManager: ClipboardManager

    class Refresher(private var activity: ActivityAttendance) : Thread() {
        private var flag = true
        override fun run() {
            while (true) {
                if (!flag) return
                activity.refresh()
                sleep(10000)
            }
        }

        fun release() {
            flag = false
        }
    }

    var refresher = Refresher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        parent = findViewById(R.id.constraintParent_layout)
        animBt = findViewById(R.id.animBt_button)
        timer = findViewById(R.id.dailyTimer_textView)
        attendanceListGridLayout = findViewById(R.id.attendanceList_gridLayout)
        topFiveListLinearLayout = findViewById(R.id.topFiveList_linearLayout)
        timeDesrip = findViewById(R.id.timeDescrip_textView)
        progressBar = findViewById(R.id.signInTime_ProcessBar)
        bottomBar = findViewById(R.id.bottom_bar)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        timer.setOnLongClickListener {
            if (logable)
                if (timer.text == "长按这段文字进行签到" || timer.text == "签到失败" || timer.text == "0") {
                } else refreshLogIn(SignAgent.UserStatus.OFFLINE)
            true
        }

        refresh(true)
        refresher.start()

        animBt.setOnClickListener {
            if (logable) {
                logable = false
                refreshLogIn(SignAgent.UserStatus.ONLINE)
                val animator = ObjectAnimator.ofFloat(animBt, "alpha", 1f, 0f).setDuration(1000)
                animator.addListener(onStart = {
                    ObjectAnimator.ofFloat(animBt, "rotation", 0f, 180f, 360f).setDuration(1000)
                        .start()
                    ObjectAnimator.ofFloat(attendanceListGridLayout, "alpha", 0f, 1f)
                        .setDuration(1000)
                        .start()
                    ObjectAnimator.ofFloat(topFiveListLinearLayout, "alpha", 0f, 1f)
                        .setDuration(1000)
                        .start()
                    ObjectAnimator.ofFloat(timer, "alpha", 0f, 1f).setDuration(1000).start()
                    ObjectAnimator.ofFloat(bottomBar, "alpha", 0f, 1f).setDuration(1000).start()
                    ObjectAnimator.ofArgb(
                        top_div,
                        "textColor",
                        getColor(R.color.colorPrimary),
                        getColor(R.color.colorWhitePure)
                    ).setDuration(1000)
                        .start()
                })
                for (item in attendanceListGridLayout.children)
                    item.isEnabled = true
                animator.addListener(onEnd = {
                    runOnUiThread {
                        parent.removeView(animBt)
                        logable = true
                    }
                })
                animator.start()
            }
        }

        nbAgent.handleStatus(VDR.userID, Consumer {
            if (it == SignAgent.UserStatus.ONLINE) {
                if (savedInstanceState != null) {
                    if (savedInstanceState.getBoolean(VDR.signOutRequestBundleKey))
                        runOnUiThread { timer.performLongClick() }
                } else runOnUiThread { animBt.performClick() }
            } else if (it == SignAgent.UserStatus.OFFLINE) {
                if (savedInstanceState != null) {
                    if (savedInstanceState.getBoolean(VDR.signInRequestBundleKey))
                        runOnUiThread { animBt.performClick() }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun refreshLogIn(status: SignAgent.UserStatus) {
        if (status == SignAgent.UserStatus.OFFLINE) {
            logable = false
            nbAgent.handleSignOut(VDR.userID, Consumer {
                runOnUiThread {
                    parent.addView(animBt)
                    val animator =
                        ObjectAnimator.ofFloat(animBt, "rotation", 360f, 180f, 0f).setDuration(1000)
                    animator.addListener(onStart = {
                        ObjectAnimator.ofFloat(attendanceListGridLayout, "alpha", 1f, 0f)
                            .setDuration(1000)
                            .start()
                        ObjectAnimator.ofFloat(topFiveListLinearLayout, "alpha", 1f, 0f)
                            .setDuration(1000)
                            .start()
                        ObjectAnimator.ofFloat(timer, "alpha", 1f, 0f).setDuration(500)
                            .start()
                        ObjectAnimator.ofFloat(bottomBar, "alpha", 1f, 0f).setDuration(500)
                            .start()
                        ObjectAnimator.ofFloat(animBt, "alpha", 0f, 1f).setDuration(500)
                            .start()
                        ObjectAnimator.ofArgb(
                            top_div,
                            "textColor",
                            getColor(R.color.colorWhitePure),
                            getColor(R.color.colorPrimary)
                        ).setDuration(1000)
                            .start()
                    })
                    animator.addListener(onEnd = {
                        timer.text = "长按这段文字进行签到"
                        progressBar.progress = 0
                        timeDesrip.text = "本周已签到0分钟，还需要1080分钟"
                        for (item in attendanceListGridLayout.children)
                            item.isEnabled = false
//                        attendanceListGridLayout.removeAllViews()
//                        topFiveListLinearLayout.removeAllViews()
                        logable = true
                    })
                    animator.start()
                }
                logedin = false
            })
        } else {
            nbAgent.handleSignOut(VDR.userID, Consumer {
                nbAgent.handleSignInResponse(VDR.userID, Consumer {
                    refresh()
                    var pos_start = 0
                    var pos_end = 0
                    val response = it!!.body!!.string()
                    pos_start = response.indexOf("userid\":") + 8
                    pos_end = response.indexOf(",\"", pos_start)
                    val userid = response.substring(pos_start, pos_end).toLong()
                    when {
                        response.contains("签到成功") -> runOnUiThread { Toast.makeText(this, "签到成功", Toast.LENGTH_SHORT).show() }
                        response.contains("成功") -> runOnUiThread { Toast.makeText(this, "签退成功", Toast.LENGTH_SHORT).show() }
                        else -> runOnUiThread {
                            Toast.makeText(
                                this,
                                "操作失败 : $response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    nbAgent.handleTime(userid, Consumer {
                        runOnUiThread {
                            timer.text = "${it!!.toInt()}"
                            if (progressBar.max < it.toInt())
                                progressBar.max = it.toInt()
                            progressBar.progress = it.toInt()
                            timeDesrip.text = "本周已签到${it.toInt()}分钟，还需要${(1080 - it.toInt())}分钟"
                        }
                    })
                    logedin = true
                })
            })
        }
    }

    private fun refresh(forced: Boolean = false) {
        if (!forced && !logedin) return
        nbAgent.handleStatus(VDR.userID, Consumer {
            if (it != SignAgent.UserStatus.ONLINE) {
                runOnUiThread { timer.performLongClick() }
            }
        })
        nbAgent.handleAttendance(Consumer { it ->
            runOnUiThread { attendanceListGridLayout.removeAllViews() }
            if (null == it || it.isEmpty()) return@Consumer
            for (member in it.sortedWith(compareBy { -it.time })) {
                val bt = Button(this)
                bt.text = member.name
                val popupMenu = PopupMenu(this, bt)
                val menu = popupMenu.menu
                menu.add(Menu.NONE, Menu.FIRST + 0, 0, "ID : ${member.id}")
                    .setOnMenuItemClickListener {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "ID",
                                member.id.toString()
                            )
                        )
                        Toast.makeText(applicationContext, "已复制到剪贴板", Toast.LENGTH_LONG).show()
                        true
                    }
                menu.add(Menu.NONE, Menu.FIRST + 1, 1, "部门 : ${member.dept}")
                    .setOnMenuItemClickListener {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "Department",
                                member.dept
                            )
                        )
                        Toast.makeText(applicationContext, "已复制到剪贴板", Toast.LENGTH_LONG).show()
                        true
                    }
                menu.add(Menu.NONE, Menu.FIRST + 2, 2, "地点 : ${member.location}")
                    .setOnMenuItemClickListener {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "Location",
                                member.location
                            )
                        )
                        Toast.makeText(applicationContext, "已复制到剪贴板", Toast.LENGTH_LONG).show()
                        true
                    }
                menu.add(Menu.NONE, Menu.FIRST + 3, 3, "签到时长 : ${member.time.toInt()}分钟")
                    .setOnMenuItemClickListener {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "Time",
                                member.time.toInt().toString() + "分钟"
                            )
                        )
                        Toast.makeText(applicationContext, "已复制到剪贴板", Toast.LENGTH_LONG).show()
                        true
                    }
                if (VDR.userID != member.id)
                    menu.add(Menu.NONE, Menu.FIRST + 4, 4, "是兄弟就举报！")
                        .setOnMenuItemClickListener {
                            nbAgent.handleComplaint(member.id, Consumer {
                                if (member.id == VDR.userID)
                                    refreshLogIn(SignAgent.UserStatus.OFFLINE)
                                else refresh()
                            })
                            Toast.makeText(applicationContext, "举报请求已发送", Toast.LENGTH_LONG)
                                .show()
                            true
                        }
                else
                    menu.add(Menu.NONE, Menu.FIRST + 4, 4, "我要举报自己").setOnMenuItemClickListener {
                        Toast.makeText(applicationContext, "请对自己好一点", Toast.LENGTH_LONG)
                            .show()
                        true
                    }

                when (member.location) {
                    "5108" -> bt.setTextColor(getColor(R.color.room5108bg))
                    "5102" -> bt.setTextColor(getColor(R.color.room5102bg))
                    "5109" -> bt.setTextColor(getColor(R.color.room5109bg))
                }
                bt.setOnClickListener {
                    popupMenu.show()
                }
                bt.width = (parent.width - 30) / 4
                runOnUiThread {
                    attendanceListGridLayout.addView(bt)
                    if (!logedin) bt.isEnabled = false
                }
            }
        })
        nbAgent.handleTopFives(Consumer {
            runOnUiThread { topFiveListLinearLayout.removeAllViews() }
            if (null == it || it.isEmpty()) return@Consumer
            for (ranker in it) {
                val bt = Button(this)
                bt.setTextColor(getColor(R.color.colorAccent))
                bt.text = "${ranker.name} ${ranker.time.toInt()}分钟"
                runOnUiThread { topFiveListLinearLayout.addView(bt) }
            }
        })
    }
}

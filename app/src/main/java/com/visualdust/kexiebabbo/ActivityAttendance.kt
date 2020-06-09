package com.visualdust.kexiebabbo

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import com.visualdust.kexiebabbo.agent.NonblockingSignAgent
import com.visualdust.kexiebabbo.agent.SignAgent
import kotlinx.android.synthetic.main.activity_attendance.*
import java.util.function.Consumer
import com.visualdust.kexiebabbo.data.Resources as VDR


class ActivityAttendance : AppCompatActivity() {
    val agent = SignAgent.getAgent()
    val nbAgent = NonblockingSignAgent.getAgent()
    var logable = true
    var logedin = false
    lateinit var parent: ConstraintLayout
    lateinit var animBt: Button
    lateinit var timer: TextView
    lateinit var attendanceListGridLayout: GridLayout
    lateinit var topFiveListLinearLayout: LinearLayout
    lateinit var timeDesrip: TextView
    lateinit var progressBar: ProgressBar
    lateinit var bottomBar: LinearLayout
    lateinit var clipboardManager: ClipboardManager

    class Refresher(var activity: ActivityAttendance) : Thread() {
        var flag = true
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
        timer = findViewById<TextView>(R.id.dailyTimer_textView)
        attendanceListGridLayout = findViewById<GridLayout>(R.id.attendanceList_gridLayout)
        topFiveListLinearLayout = findViewById<LinearLayout>(R.id.topFiveList_linearLayout)
        timeDesrip = findViewById<TextView>(R.id.timeDescrip_textView)
        progressBar = findViewById<ProgressBar>(R.id.signInTime_ProcessBar)
        bottomBar = findViewById(R.id.bottom_bar)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        timer.setOnLongClickListener {
            if (logable) {
                if (timer.text == "长按这段文字进行签到" || timer.text == "签到失败" || timer.text == "0") {
                }
//                refreshLogIn(SignAgent.UserStatus.ONLINE)
                else refreshLogIn(SignAgent.UserStatus.OFFLINE)
            }
            true
        }

        logedin = true
        refresh()
        logedin = false
        refresher.start()
        nbAgent.handleStatus(VDR.userID, Consumer {
            if (it == SignAgent.UserStatus.ONLINE) {
//                runOnUiThread {
//                    parent.removeView(animBt)
//                    timer.alpha = 1F
//                    attendanceListGridLayout.alpha = 1F
//                    topFiveListLinearLayout.alpha = 1F
//                    top_div.setTextColor(getColor(R.color.colorWhitePure))
//                    bottomBar.alpha = 1F
//                }
//                refreshLogIn(SignAgent.UserStatus.ONLINE)
                runOnUiThread { animBt.performClick() }
            }
        })

        animBt.setOnClickListener {
            if (logable) {
                logable = false
                refreshLogIn(SignAgent.UserStatus.ONLINE)
                var animator = ObjectAnimator.ofFloat(animBt, "alpha", 1f, 0f).setDuration(1000)
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
                animator.addListener(onEnd = {
                    runOnUiThread {
                        parent.removeView(animBt)
                        logable = true
                    }
                })
                animator.start()
            }
        }
    }

    private fun refreshLogIn(status: SignAgent.UserStatus) {
        if (status == SignAgent.UserStatus.OFFLINE) {
            logable = false
            nbAgent.handleSignOut(VDR.userID, Consumer {
                runOnUiThread {
                    parent.addView(animBt)
                    var animator =
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
                    if (response.contains("签到成功"))
                        runOnUiThread { Toast.makeText(this, "签到成功", Toast.LENGTH_SHORT).show() }
                    else if (response.contains("成功"))
                        runOnUiThread { Toast.makeText(this, "签退成功", Toast.LENGTH_SHORT).show() }
                    else runOnUiThread {
                        Toast.makeText(
                            this,
                            "操作失败 : $response",
                            Toast.LENGTH_SHORT
                        ).show()
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

    private fun refresh() {
        if (!logedin) return
        nbAgent.handleStatus(VDR.userID, Consumer {
            if (it != SignAgent.UserStatus.ONLINE) {
                runOnUiThread { timer.performLongClick() }
            }
        })
        nbAgent.handleAttendance(Consumer {
            runOnUiThread { attendanceListGridLayout.removeAllViews() }
            for (member in it!!.sortedWith(compareBy({ -it.time }))) {
                val bt = Button(this)
                bt.setText(member.name)
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
                runOnUiThread { attendanceListGridLayout.addView(bt) }
            }
        })
        nbAgent.handleTopFives(Consumer {
            runOnUiThread { topFiveListLinearLayout.removeAllViews() }
            for (ranker in it!!) {
                val bt = Button(this)
                bt.setTextColor(getColor(R.color.colorAccent))
                bt.setText("${ranker.name} ${ranker.time.toInt()}分钟")
                runOnUiThread { topFiveListLinearLayout.addView(bt) }
            }
        })
    }
}

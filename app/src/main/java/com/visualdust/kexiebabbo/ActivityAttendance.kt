package com.visualdust.kexiebabbo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.visualdust.kexiebabbo.agent.NonblockingSignAgent
import com.visualdust.kexiebabbo.agent.SignAgent
import java.util.function.Consumer
import com.visualdust.kexiebabbo.data.Resources as VDR


class ActivityAttendance : AppCompatActivity() {
    val agent = SignAgent.getAgent()
    val nbAgent = NonblockingSignAgent.getAgent()
    lateinit var timer: TextView
    lateinit var attendanceListGridLayout: GridLayout
    lateinit var topFiveListLinearLayout: LinearLayout
    lateinit var timeDesrip: TextView
    lateinit var progressBar: ProgressBar
    lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        timer = findViewById<TextView>(R.id.dailyTimer_textView)
        attendanceListGridLayout = findViewById<GridLayout>(R.id.attendanceList_gridLayout)
        topFiveListLinearLayout = findViewById<LinearLayout>(R.id.topFiveList_linearLayout)
        timeDesrip = findViewById<TextView>(R.id.timeDescrip_textView)
        progressBar = findViewById<ProgressBar>(R.id.signInTime_ProcessBar)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        timeDesrip.setOnClickListener {
            if (timeDesrip.text == "本周已签到0分钟，还需要1080分钟"){
                runOnUiThread {
                    Toast.makeText(this, "你还没有签到，无法查看你的签到时间", Toast.LENGTH_SHORT).show()
                }
            } else{
                    nbAgent.handleTime(VDR.userID, Consumer {
                        runOnUiThread {
                            // for testing
                            Toast.makeText(this, "你本周的签到时间为:${it}分钟", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        timer.setOnLongClickListener {
            if (timer.text == "长按这段文字进行签到" || timer.text == "签到失败" || timer.text == "0")
                refreshLogIn(SignAgent.UserStatus.ONLINE)
            else refreshLogIn(SignAgent.UserStatus.OFFLINE)
            true
        }

        nbAgent.handleStatus(VDR.userID, Consumer {
            if (it == SignAgent.UserStatus.ONLINE)
                refreshLogIn(SignAgent.UserStatus.ONLINE)
        })

        refresh()

        Thread {
            while (true) {
                Thread.sleep(10000)
                refresh()
            }
        }.start()
        refresh()
    }

    private fun refreshLogIn(status: SignAgent.UserStatus) {
        if (status == SignAgent.UserStatus.OFFLINE) {
            nbAgent.handleSignOut(VDR.userID, Consumer {
                timer.text = "长按这段文字进行签到"
                progressBar.progress = 0
                timeDesrip.text = "本周已签到0分钟，还需要1080分钟"
            })
            refresh()
        } else {
            nbAgent.handleSignOut(VDR.userID, Consumer {
                nbAgent.handleSignInResponse(VDR.userID, Consumer {
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
                    runOnUiThread {
                        nbAgent.handleTime(userid, Consumer {
                            timer.text = "${it.toInt()}"
                            if (progressBar.max < it.toInt())
                                progressBar.max =it.toInt()
                            progressBar.progress = it.toInt()
                            timeDesrip.text = "本周已签到${it.toInt()}分钟，还需要${(1080 - it.toInt())}分钟"
                        })
                    }
                    refresh()
                })
            })
        }
    }

    fun refresh() {
        nbAgent.handleAttendance(Consumer {
            runOnUiThread { attendanceListGridLayout.removeAllViews() }
            for (member in it!!) {
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
                if (VDR.userID != member.id)
                    menu.add(Menu.NONE, Menu.FIRST + 3, 3, "是兄弟就举报！")
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
                    menu.add(Menu.NONE, Menu.FIRST + 3, 3, "我要举报自己").setOnMenuItemClickListener {
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

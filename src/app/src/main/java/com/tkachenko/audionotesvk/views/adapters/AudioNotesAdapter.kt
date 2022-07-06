package com.tkachenko.audionotesvk.views.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.models.AudioNote
import com.tkachenko.audionotesvk.utils.Utils


private const val TAG = "AudioNotesAdapter"

class AudioNotesAdapter: RecyclerView.Adapter<AudioNotesAdapter.AudioNoteHolder>() {
    interface Callback {
        fun onClickBtnPlay(title: String, position: Int)
        fun onClickBtnStop(title: String, position: Int)
        fun onClickBtnUpload(title: String, position: Int)
    }

    private val audioNotes: MutableList<AudioNote> = mutableListOf()
    private var playingPosition: Int? = null
    private lateinit var callback: Callback

    fun attachCallback(callback: Callback) {
        this.callback = callback
    }

    fun startPlay(position: Int) {
        audioNotes[position].isPlay = true
        notifyItemChanged(position)
    }

    fun stopPlay(position: Int) {
        audioNotes[position].isPlay = false
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(audioNotes: List<AudioNote>) {
        this.audioNotes.clear()
        this.audioNotes.addAll(audioNotes)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeData(position: Int): String {
        val title = audioNotes[position].title
        audioNotes.removeAt(position)
        notifyDataSetChanged()
        return title
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioNoteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio_note, parent,false)
        return AudioNoteHolder(view = view)
    }

    override fun onBindViewHolder(holder: AudioNoteHolder, position: Int) {
        val audioNote = audioNotes[position]
        holder.bind(audioNote)
    }

    override fun getItemCount() = audioNotes.size

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class AudioNoteHolder(view: View): RecyclerView.ViewHolder(view), View.OnTouchListener {
        private val title: TextView = view.findViewById(R.id.title)
        private val btnPlay: ImageView = view.findViewById(R.id.btn_play)
        private val btnPause: ImageView = view.findViewById(R.id.btn_pause)
        private val btnUpload: ImageView = view.findViewById(R.id.btn_upload)
        private val duration: TextView = view.findViewById(R.id.duration)
        private val currentDuration: TextView = view.findViewById(R.id.current_duration)
        private val date: TextView = view.findViewById(R.id.date)
        private val parent: LinearLayout = view.findViewById(R.id.parent)
        private val progressIndicator: LinearProgressIndicator = view.findViewById(R.id.progress_indicator)

        init {
            parent.apply {
                outlineProvider = ViewOutlineProvider.BACKGROUND
                clipToOutline = true
            }
        }

        @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
        fun bind(model: AudioNote) {
            title.text = model.title
            duration.text = model.duration
            date.text = model.date
            title.isSelected = true

            if (model.isPlay) {
                btnPlay.visibility = View.GONE
                btnPause.visibility = View.VISIBLE
                progressIndicator.visibility = View.VISIBLE
                currentDuration.visibility = View.VISIBLE
            } else {
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.GONE
                progressIndicator.visibility = View.INVISIBLE
                currentDuration.visibility = View.GONE
            }

            btnPlay.setOnClickListener {
                callback.onClickBtnPlay(model.title, adapterPosition)
                playingPosition?.let {
                    audioNotes[playingPosition!!].isPlay = false
                    progressIndicator.progress = 0
                    notifyItemChanged(playingPosition!!)
                }
                audioNotes[adapterPosition].isPlay = true
                playingPosition = adapterPosition
                notifyItemChanged(adapterPosition)
            }

            btnPause.setOnClickListener {
                callback.onClickBtnStop(model.title, adapterPosition)
                audioNotes[adapterPosition].isPlay = false
                notifyItemChanged(adapterPosition)
            }

            btnUpload.setOnClickListener {
                callback.onClickBtnUpload(model.title, adapterPosition)
            }

            btnPlay.setOnTouchListener(this)
            btnPause.setOnTouchListener(this)
            btnUpload.setOnTouchListener(this)
        }

        fun setProgressIndicatorMax(max: Int) {
            progressIndicator.max = max
        }

        @SuppressLint("SetTextI18n")
        fun updateProgressIndicator(progress: Int) {
            progressIndicator.setProgressCompat(progress, true)
            currentDuration.text = "${Utils.getTimeStringFromInt(progress)} / "
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            when (view?.id) {
                R.id.btn_play -> {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            (view as ImageView).imageAlpha = 200
                        }
                        MotionEvent.ACTION_UP -> {
                            (view as ImageView).imageAlpha = 255
                            view.invalidate()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            (view as ImageView).imageAlpha = 255
                            view.invalidate()
                        }
                    }
                }
                R.id.btn_pause -> {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            (view as ImageView).imageAlpha = 200
                        }
                        MotionEvent.ACTION_UP -> {
                            (view as ImageView).imageAlpha = 255
                            view.invalidate()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            (view as ImageView).imageAlpha = 255
                            view.invalidate()
                        }
                    }
                }
                R.id.btn_upload -> {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            (view as ImageView).imageAlpha = 200
                        }
                        MotionEvent.ACTION_UP -> {
                            (view as ImageView).imageAlpha = 255
                            view.invalidate()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            (view as ImageView).imageAlpha = 255
                            view.invalidate()
                        }
                    }
                }
            }
            return false
        }
    }
}
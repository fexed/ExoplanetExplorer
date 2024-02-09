package com.fexed.exoplanetexplorer

import com.google.gson.annotations.SerializedName


data class Page (
  @SerializedName("pageid"  ) var pageid  : Int?    = null,
  @SerializedName("ns"      ) var ns      : Int?    = null,
  @SerializedName("title"   ) var title   : String? = null,
  @SerializedName("extract" ) var extract : String? = null
)
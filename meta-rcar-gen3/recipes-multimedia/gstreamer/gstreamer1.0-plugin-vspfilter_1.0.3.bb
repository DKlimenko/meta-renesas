SUMMARY = "GStreamer VSP filter plugin"
SECTION = "multimedia"
LICENSE = "LGPLv2.1+"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c"

COMPATIBLE_MACHINE = "(salvator-x|ulcb|ebisu)"

RENESAS_VSPFILTER_URL ?= "gitsm://github.com/renesas-rcar/gst-plugin-vspfilter.git;branch=RCAR-GEN3e/1.0.3;protocol=https"

SRC_URI = " \
    ${RENESAS_VSPFILTER_URL} \
"

SRCREV = "e538bc3b400aac44a590099d52273f75b9f54ff7"

S = "${WORKDIR}/git"


inherit meson pkgconfig

DEPENDS += "gstreamer1.0 gstreamer1.0-plugins-base pkgconfig"

FILES:${PN} = " \
    ${libdir}/gstreamer-1.0/libgstvspfilter.so \
"

FILES:${PN}-dev = "${libdir}/gstreamer-1.0/libgstvspfilter.la"

FILES:${PN}-staticdev = "${libdir}/gstreamer-1.0/libgstvspfilter.a"

FILES:${PN}-dbg = " \
    ${libdir}/gstreamer-1.0/.debug \
    ${prefix}/src \
"

RDEPENDS:${PN} = "kernel-module-vsp2driver gstreamer1.0-plugin-vspfilter-config"

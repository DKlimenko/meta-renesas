DESCRIPTION = "Linux kernel for the R-Car Generation 3 based board"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require include/avb-control.inc
require include/iccom-control.inc
require recipes-kernel/linux/linux-yocto.inc
require include/cas-control.inc
require include/adsp-control.inc

COMPATIBLE_MACHINE = "salvator-x|h3ulcb|m3ulcb|m3nulcb|ebisu|draak"

RENESAS_BSP_URL = " \
    git://github.com/renesas-rcar/linux-bsp.git"
BRANCH = "v5.10/rcar-5.0.0.rc4"
SRCREV = "301d2c636929be96f3d87b1b5d287f87ed67a7be"

SRC_URI = "${RENESAS_BSP_URL};nocheckout=1;branch=${BRANCH}"

LINUX_VERSION ?= "5.10.0"
PV = "${LINUX_VERSION}+git${SRCPV}"
PR = "r1"

# For generating defconfig
KCONFIG_MODE = "--alldefconfig"
KBUILD_DEFCONFIG = "defconfig"

SRC_URI_append = " \
    file://touch.cfg \
    ${@oe.utils.conditional("USE_AVB", "1", " file://usb-video-class.cfg", "", d)} \
    file://add-intc_ex-for-r8a77961.patch \
    file://cpufreq-boost-for-rcar-5.0.0.rc4.patch \
    file://0001-arm64-dts-r8a77961-Fix-video-codec-relation-node.patch \
"

# Add module.lds
SRC_URI_append = " \
    file://0001-scripts-Add-module.lds-to-fix-out-of-tree-modules-bu.patch \
"

# Add OP-TEE node for R8A77995
SRC_URI_append = " \
    file://0001-arm64-dts-r8a77995-Add-optee-node.patch \
"

# Enable RPMSG_VIRTIO depend on ICCOM
SUPPORT_ICCOM = " \
    file://iccom.cfg \
"

SRC_URI_append = " \
    ${@oe.utils.conditional("USE_ICCOM", "1", "${SUPPORT_ICCOM}", "", d)} \
"

# Add SCHED_DEBUG config fragment to support CAS
SRC_URI_append = " \
    ${@oe.utils.conditional("USE_CAS", "1", " file://capacity_aware_migration_strategy.cfg", "",d)} \
"

# Add ADSP ALSA driver
SUPPORT_ADSP_ASOC = " \
    file://ADSP-add-ADSP-sound-driver-source-final.patch \
    file://update_kernel_device_tree_and_build_config_rc4.patch \
    file://adsp.cfg \
"

SRC_URI_append = " \
    ${@oe.utils.conditional("USE_ADSP", "1", "${SUPPORT_ADSP_ASOC}", "", d)} \
"

# Install USB3.0 firmware to rootfs
USB3_FIRMWARE_V2 = "https://git.kernel.org/pub/scm/linux/kernel/git/firmware/linux-firmware.git/plain/r8a779x_usb3_v2.dlmem;md5sum=645db7e9056029efa15f158e51cc8a11"
USB3_FIRMWARE_V3 = "https://git.kernel.org/pub/scm/linux/kernel/git/firmware/linux-firmware.git/plain/r8a779x_usb3_v3.dlmem;md5sum=687d5d42f38f9850f8d5a6071dca3109"

SRC_URI_append = " \
    ${USB3_FIRMWARE_V2} \
    ${USB3_FIRMWARE_V3} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'usb3', 'file://usb3.cfg', 'file://disable_fw_loader_user_helper.cfg', d)} \
"

SRC_URI_append_salvator-x = " \
    file://r8a77951-salvator-xs-xen.dts;subdir=git/arch/${ARCH}/boot/dts/renesas \
"

KERNEL_DEVICETREE_append_salvator-x = "renesas/r8a77951-salvator-xs-xen.dtb"

SRC_URI_append = " \
    file://0001-dma-mapping-handle-vmalloc-addresses-in-dma_common_-.patch \
"

do_download_firmware () {
    install -d ${STAGING_KERNEL_DIR}/firmware
    install -m 755 ${WORKDIR}/r8a779x_usb3_v*.dlmem ${STAGING_KERNEL_DIR}/firmware
}

addtask do_download_firmware after do_configure before do_compile

do_compile_kernelmodules_append () {
    if (grep -q -i -e '^CONFIG_MODULES=y$' ${B}/.config); then
        # 5.10+ kernels have module.lds that we need to copy for external module builds
        if [ -e "${B}/scripts/module.lds" ]; then
            install -Dm 0644 ${B}/scripts/module.lds ${STAGING_KERNEL_BUILDDIR}/scripts/module.lds
        fi
    fi
}

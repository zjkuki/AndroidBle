/*
 * Copyright (c) 2017. xiaoyunfei
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.eson.liteble.activity.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.e.ble.bean.BLEDevice;
import com.e.ble.scan.BLEScanCfg;
import com.e.ble.scan.BLEScanListener;
import com.e.ble.scan.BLEScanner;
import com.e.ble.util.BLEConstant;
import com.e.ble.util.BLEError;

import org.eson.liteble.MyApplication;
import org.eson.liteble.R;
import org.eson.liteble.activity.BleDetailActivity;
import org.eson.liteble.activity.MainActivity;
import org.eson.liteble.adapter.ScanBLEItem;
import org.eson.liteble.service.BleService;
import org.eson.liteble.util.BondedDeviceBean;
import org.eson.liteble.util.BondedDeviceUtil;
import org.eson.liteble.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kale.adapter.CommonRcvAdapter;
import kale.adapter.item.AdapterItem;

/**
 * @package_name org.eson.liteble.activity.fragment
 * @name ${name}
 * <p>
 * Created by xiaoyunfei on 2017/5/5.
 * @description 扫描设备界面
 */

public class DeviceScanFragment extends BaseFragment {
    private RecyclerView mListView;

    private List<BLEDevice> deviceList = new ArrayList<>();
    private CommonRcvAdapter<BLEDevice> scanBLEAdapter;
    private ProgressDialog m_pDialog;

    private BLEDevice selectDevice = null;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_scan_device;
    }

    @Override
    protected void initViews() {

        mListView = findView(R.id.listview);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListView.setItemAnimator(new DefaultItemAnimator());
        scanBLEAdapter = new CommonRcvAdapter<BLEDevice>(deviceList) {
            @NonNull
            @Override
            public AdapterItem createItem(Object o) {
                return new ScanBLEItem(getActivity(), mOnClickListener);
            }
        };
        mListView.setAdapter(scanBLEAdapter);
    }

    ScanBLEItem.ItemClickListener mOnClickListener = new ScanBLEItem.ItemClickListener() {

        @Override
        public void onItemClick(int position) {
            selectDevice = deviceList.get(position);
            MyApplication.getInstance().setCurrentShowDevice(selectDevice.getMac());
            BleService.get().connectionDevice(getActivity(), selectDevice.getMac());

            showProgress("正在连接设备：" + selectDevice.getName());
        }
    };


    @Override
    public void onPause() {
        BLEScanner.get().stopScan();
        super.onPause();
    }

    /**
     * 显示等待框
     *
     * @param msg
     */
    public void showProgress(String msg) {
        if (m_pDialog == null) {
            m_pDialog = new ProgressDialog(getActivity());
            m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            m_pDialog.setIndeterminate(false);
            m_pDialog.setCancelable(true);
        }
        if (m_pDialog.isShowing()) {
            return;
        }

        m_pDialog.setMessage(msg);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) {
            return;
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_pDialog.show();
            }
        });

    }

    public void hideProgress() {

        if (m_pDialog == null) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_pDialog.dismiss();
            }
        });

    }

    /**
     * 扫描蓝牙设备
     */
    private void searchDevice() {
        showProgress("搜索设备中。。。。");
        BLEScanCfg scanCfg = new BLEScanCfg.ScanCfgBuilder(
                MyApplication.getInstance().getConfigShare().getConnectTime())
                //.addUUIDFilter(UUID.fromString("6E401892-B5A3-F393-E0A9-E50E24DCCA9E"))
                .addUUIDFilter(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"))
                .builder();
        BLEScanner.get().startScanner(scanCfg, new BLEScanListener() {
            @Override
            public void onScanning(BLEDevice device) {
                hideProgress();
                addScanBLE(device);
            }


            @Override
            public void onScannerStop() {
                try {
                    ((MainActivity) getActivity()).reSetMenu();
                    ToastUtil.showShort(getActivity(), "扫描结束");
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onScannerError(int errorCode) {
                hideProgress();
                if (errorCode == BLEError.BLE_CLOSE) {
                    ToastUtil.showShort(getActivity(), "蓝牙未打开，请打开蓝牙后重试");
                } else {
                    ToastUtil.showShort(getActivity(), "扫描出现异常");
                }
            }

        });


    }

    public void addScanBLE(final BLEDevice bleDevice) {

        if (isExitDevice(bleDevice)) {
            updateDevice(bleDevice);
            scanBLEAdapter.notifyDataSetChanged();
            return;
        }
        deviceList.add(bleDevice);
        scanBLEAdapter.notifyItemInserted(deviceList.size());
    }


    private boolean isExitDevice(BLEDevice device) {
        for (BLEDevice bleDevice : deviceList) {
            if (bleDevice.getMac().equals(device.getMac())) {
                return true;
            }
        }
        return false;
    }

    private void updateDevice(BLEDevice device) {
        for (BLEDevice bleDevice : deviceList) {
            if (bleDevice.getMac().equals(device.getMac())) {
                bleDevice.setRssi(device.getRssi());
                bleDevice.setScanRecord(device.getScanRecord());
            }
        }
    }


    @Override
    public void onBleStateChange(String mac, int state) {
        super.onBleStateChange(mac, state);

        switch (state) {
            case BLEConstant.Connection.STATE_CONNECT_CONNECTED:
            case BLEConstant.Connection.STATE_CONNECT_SUCCEED:

                BondedDeviceUtil.get().addBondDevice(mac);
                BondedDeviceBean bondedDeviceBean = BondedDeviceUtil.get().getDevice(mac);
                if (mac.equals(selectDevice.getMac())) {

                    bondedDeviceBean.setName(selectDevice.getName());
                }
                bondedDeviceBean.setConnected(true);

                startToNext();
                break;
            case BLEConstant.Connection.STATE_CONNECT_FAILED:
                hideProgress();
                ToastUtil.showShort(getActivity(), "设备连接失败");
                break;
            case BLEConstant.State.STATE_CONNECTED:
                hideProgress();
                ToastUtil.showShort(getActivity(), "设备连接成功");

                break;
            case BLEConstant.State.STATE_DIS_CONNECTED:
                hideProgress();
                ToastUtil.showShort(getActivity(), "设备断开");
                break;
            default:
                break;

        }
    }

    /**
     * 跳转的详情界面
     */
    private void startToNext() {
        if (!MyApplication.getInstance().isForeground(MainActivity.class.getName())) {
            return;
        }
        hideProgress();

        ToastUtil.showShort(getActivity(), "连接成功");
        Intent intent = new Intent(getActivity(), BleDetailActivity.class);
        intent.putExtra("mac", selectDevice.getMac());
        intent.putExtra("name", selectDevice.getName());
        startActivity(intent);

    }

    public void stopScanner() {

        BLEScanner.get().stopScan();
    }

    public void startScanner() {
        deviceList.clear();
        scanBLEAdapter.notifyDataSetChanged();
        searchDevice();
    }
}
